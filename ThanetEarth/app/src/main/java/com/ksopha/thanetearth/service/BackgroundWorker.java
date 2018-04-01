package com.ksopha.thanetearth.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import com.ksopha.thanetearth.checker.MeasurementChecker;
import com.ksopha.thanetearth.ormObject.Log;
import com.ksopha.thanetearth.ormObject.Sensor;
import com.ksopha.thanetearth.ormObject.SensorBasicData;
import com.ksopha.thanetearth.sensor.SensorAPIWorker;
import com.ksopha.thanetearth.ormObject.SensorHistory;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import io.realm.Realm;
import io.realm.Sort;


/**
 * Service to run http requests and save responses to database using SugarORM
 * Created by Kelvin Sopha on 25/03/18.
 */

public class BackgroundWorker extends Service {

    private static final int BASIC_UPDATE_SECONDDS_INTERVAL = 60 * 1; // 10 minutes
    private static final int HISTORY_UPDATE_SECONDDS_INTERVAL = 60 * 60; // 1 hour
    private long startTime;
    public static final String ACTION = "com.ksopha.thanetearth.service.BackgroundWorker";
    private SensorAPIWorker sensorAPIWorker;
    private List<Sensor> sensors;
    private ServiceHandler serviceHandler;
    private LooperThread thread;
    private boolean pulledHistory;
    private String [] types = {"temperature", "moisture", "tds", "light"};
    private String[] sites = {"gh1", "gh2", "gh3", "outside"};
    public static boolean updatedSiteHistory[]= new boolean[4];
    private MeasurementChecker measurementChecker;
    private NotificationHelper notificationHelper;



    /**
     * called at creation of Service
     */
    @Override
    public void onCreate() {
        super.onCreate();

        notificationHelper = new NotificationHelper(this);

        measurementChecker = new MeasurementChecker();

        // save time for timer
        startTime = System.currentTimeMillis();

        sensorAPIWorker = new SensorAPIWorker();

        serviceHandler = new ServiceHandler();

        thread = new LooperThread(this, serviceHandler);
        thread.start();
    }



    /**
     * called when service starts
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // run work on background thread
        android.util.Log.i("EE","pulling current sensor measurements");

        if(!thread.isAlive()){
            thread.start();
        }

        return START_STICKY;

    }


    /**
     * tell service get site zones as sensors
     */
    private void getSiteIdZones(){
        if(sensors==null || sensors.isEmpty()) {
            // clear since data might change on server
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.where(Sensor.class).findAll().deleteAllFromRealm();
            realm.commitTransaction();

            sensors = sensorAPIWorker.getSensors();

            if(sensors!=null && !sensors.isEmpty()){
                //save sensors
                realm.beginTransaction();
                realm.insert(sensors);
                realm.commitTransaction();
            }
            realm.close();

        }
    }


    /**
     * save current measurement data for a site in db, store alert Logs, then notify if there are alerts
     * @param handler handler to send message with
     */
    private void checkAndSaveBasicSiteData(Handler handler){

        List<Log> toRemove = new ArrayList<>();

        for(int i=0;i< sites.length; i++){

            boolean siteDataSaved = false;

            final List<SensorBasicData> siteData = sensorAPIWorker.getGreenHouseBasicData(sites[i], sensors);

            toRemove.addAll(checkForUnusualData(siteData));

            if(siteData != null && siteData.size()>0){
                Realm realm = Realm.getDefaultInstance();
                // save records to database
                realm.beginTransaction();
                realm.insertOrUpdate(siteData);
                realm.commitTransaction();
                realm.close();

                // set to true so we can notify Main activity
                siteDataSaved = true;
            }

            // if saved then send message
            if(siteDataSaved){

                Message msg1 = new Message();
                msg1.arg1=i;
                handler.sendMessage(msg1);
            }

        }

        android.util.Log.e("new alerts total:", toRemove.size()+"");
        // if there are not alert logs to save, save, then send notification about new alerts
        if(toRemove.size() > 0){

            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.insert(toRemove);
            realm.commitTransaction();
            realm.close();

            // to tell activity to update alerts if visible
            Message msg1 = new Message();
            msg1.arg1=4;
            handler.sendMessage(msg1);
            notificationHelper.sendNotification();
        }
    }



    /**
     * Method checks
     * @param siteData
     */
    private List<Log> checkForUnusualData(List<SensorBasicData> siteData) {


        List<Log> toRemove = new ArrayList<>();

        long dateUnix = System.currentTimeMillis();

        for (SensorBasicData data : siteData) {

            Sensor sensor = data.getSensor();

            String sensorName = sensor.getName();

            for (int i = 0; i < sites.length; i++) {

                if (sensor.getSite().equals(sites[i])) {

                    String state1 = measurementChecker.checkTempAndGetString(i, data.getTemperature());
                    String state2 = measurementChecker.checkSoilMoistureAndGetString(i, data.getMoisture());
                    String state3 = measurementChecker.checkSoilNutrientAndGetString(i, data.getTds());
                    String state4 = measurementChecker.checkLuxAndGetString(i, data.getLight());
                    String state5 = measurementChecker.checkBatteryAndGetString(data.getBatteryLevel());

                    if (state1 != null) {
                        Log log = new Log(dateUnix, "Greenhouse " + (i + 1) + ": " + sensorName +
                                " sensor temperature is " + data.getTemperature() + " °C (" + state1 + ")");
                        if(checkIfCanNotifyLog(log)){
                            toRemove.add(log);
                        }

                    }
                    if (state2 != null) {
                        Log log = new Log(dateUnix, "Greenhouse " + (i + 1) + ": " + sensorName +
                                " sensor soil moisture is " + data.getMoisture() + " % (" + state2 + ")");
                        if(checkIfCanNotifyLog(log)){
                            toRemove.add(log);
                        }
                    }
                    if (state3 != null) {
                        Log log = new Log(dateUnix, "Greenhouse " + (i + 1) + ": " + sensorName +
                                " sensor TDS(total dissolved solids) is " + data.getTds() + " ppm (" + state3 + ")");
                        if(checkIfCanNotifyLog(log)){
                            toRemove.add(log);
                        }

                    }
                    if (state4 != null) {
                        Log log = new Log(dateUnix, "Greenhouse " + (i + 1) + ": " + sensorName +
                                " sensor LUX(light intensity) is " + data.getTds() + " lx (" + state4 + ")");
                        if(checkIfCanNotifyLog(log)){
                            toRemove.add(log);
                        }

                    }
                    if (state5 != null) {
                        Log log = new Log(dateUnix, "Greenhouse " + (i + 1) + ": " + sensorName +
                                " sensor battery level is " + data.getTds() + " % (" + state5 + ")");
                        if(checkIfCanNotifyLog(log)){
                            toRemove.add(log);
                        }

                    }
                }

            }
        }
        return toRemove;
    }


    /**
     * check if the log was saved earlier < 1 day
     * @param k log
     * @return if was saved
     */
    private boolean checkIfCanNotifyLog(Log k){

        long date1, date2;

        // check if exist another log with same msg and date (excluding time) inserted earlier
        Realm realm = Realm.getDefaultInstance();
        Log earlier = realm.where(Log.class).equalTo("msg", k.getMsg())
                .sort("date", Sort.DESCENDING).findFirst();

        date1 = k.getDate();
        date2 = earlier==null ? 0: earlier.getDate();
        realm.close();

        // if log not stored, or if same log was stored earlier (more than 1 day) we can notify it
        return (date2==0 || (earlier!=null && isDateBiggerByDay(date1 ,date2)));
    }





    private boolean isDateBiggerByDay(Long date1, Long date2){

        long daysDifferrence = (date1-date2) / (24 * 60 * 60 * 1000);
        return daysDifferrence>=1;
    }


    /**
     * saves the history of a greenhouse
     * @param site id of greenhouse
     * @return if success
     */
    private boolean saveGreenhouseHistory(String site){

        for(String type: types){

            List<SensorHistory> siteData = sensorAPIWorker.getGreenhouseHistoryData(
                    "/"+type+"/hour", type, site , sensors);

            if(siteData==null){
                pulledHistory= false;
                return false;
            }

            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            // save to database
            realm.insertOrUpdate(siteData);
            realm.commitTransaction();
            realm.close();
        }

        return  true;
    }


    /**
     * called before service is destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        android.util.Log.i("R",  "service done");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * Class for a single thread that will execute tasks in background on service
     */
    class LooperThread extends Thread {

        private WeakReference<BackgroundWorker> mService;
        private ServiceHandler handler;
        private boolean isRunning;

        LooperThread(BackgroundWorker service, ServiceHandler handler) {
            mService = new WeakReference<BackgroundWorker>(service);
            this.handler = handler;
        }

        public void run() {
            isRunning = true;
            Looper.prepare();

            while(isRunning){

                getSiteIdZones();
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                realm.where(SensorBasicData.class).findAll().deleteAllFromRealm();
                realm.commitTransaction();
                realm.close();
                checkAndSaveBasicSiteData(handler);


                // if the startTime is larger in seconds than interval to get history data
                if((System.currentTimeMillis()-startTime)/1000 >= HISTORY_UPDATE_SECONDDS_INTERVAL){
                    // we set to false so we can send a request
                    pulledHistory = false;
                    //
                }


                if(!pulledHistory) {

                    updatedSiteHistory[0]=updatedSiteHistory[1]=updatedSiteHistory[2]=updatedSiteHistory[3]=false;
                    Realm realm2 = Realm.getDefaultInstance();
                    realm2.beginTransaction();
                    realm2.where(SensorHistory.class).findAll().deleteAllFromRealm();
                    realm2.commitTransaction();
                    realm2.close();
                    pulledHistory=true;

                    for(int i=0;i< sites.length; i++){

                        if(saveGreenhouseHistory(sites[i])){

                            updatedSiteHistory[i] = true;

                            Message msg2 = new Message();
                            msg2.arg1=10+i;
                            handler.sendMessage(msg2);

                        }
                    }

                    // reset the start time for timer
                    startTime = System.currentTimeMillis();
                }


                try {
                    Thread.sleep(1000 * BASIC_UPDATE_SECONDDS_INTERVAL);
                }catch(Exception e){}
            }

            // start a loop that listens for msg
            Looper.loop();
        }
    }


    /**
     * Handler to handle messages sent by the thread to the service
     */
    class ServiceHandler extends Handler {

        @Override
        public void handleMessage(Message msg)
        {
            android.util.Log.i("service", msg.arg1+ " : message from thread");

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION);

            // process incoming messages here
            if(msg.arg1==-1){
                // stop the service
                getLooper().quit();
            }
            else if(msg.arg1==0 || msg.arg1==1 || msg.arg1==2 || msg.arg1==3) {

                // means tell fragments to update
                // tell Main ui to update
                broadcastIntent.putExtra("current", msg.arg1);
                LocalBroadcastManager.getInstance(BackgroundWorker.this).sendBroadcast(broadcastIntent);

            }
            else if(msg.arg1==4){

                // means tell alerts fragment to update
                broadcastIntent.putExtra("current", msg.arg1);
                LocalBroadcastManager.getInstance(BackgroundWorker.this).sendBroadcast(broadcastIntent);

            }
            else if(msg.arg1==10 || msg.arg1==11 || msg.arg1==12 || msg.arg1==13) {

                // means tell fragments to update
                // tell Main ui to update
                broadcastIntent.putExtra("history", msg.arg1-10);
                LocalBroadcastManager.getInstance(BackgroundWorker.this).sendBroadcast(broadcastIntent);
            }
        }
    }

}
