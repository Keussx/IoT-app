package com.ksopha.thanetearth.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.ksopha.thanetearth.ormObject.Sensor;
import com.ksopha.thanetearth.ormObject.SensorBasicData;
import com.ksopha.thanetearth.sensor.SensorAPIWorker;
import com.ksopha.thanetearth.ormObject.SensorHistory;
import com.orm.SugarRecord;
import java.lang.ref.WeakReference;
import java.util.List;


/**
 * Service to run http requests and save responses to database using SugarORM
 * Created by Kelvin Sopha on 25/03/18.
 */

public class BackgroundWorker extends Service {

    private static final int BASIC_UPDATE_SECONDDS_INTERVAL = 60 * 10; // 10 minutes
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



    /**
     * called at creation of Service
     */
    @Override
    public void onCreate() {
        super.onCreate();

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
        Log.i("EE","pulling current sensor measurements");

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
            Sensor.deleteAll(Sensor.class);
            sensors = sensorAPIWorker.getSaveAndReturnSensors();
        }
    }


    /**
     * save current measurement data for a site in db
     * @param site site too get measurements for
     * @return if success
     */
    private boolean saveBasicSiteData(String site){

        final List<SensorBasicData> siteData = sensorAPIWorker.getGreenHouseBasicData(site, sensors);

        if(siteData==null || siteData.isEmpty())
            return false;

        for(Sensor sensor:sensors){
            if(sensor.getSite()==site)
            SensorBasicData.executeQuery("DELETE FROM SENSOR_BASIC_DATA WHERE Sensor="+sensor.getId()+"");
        }

        // save records to database
        SugarRecord.saveInTx(siteData);

        return true;
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

            // save to database
            for(SensorHistory s:siteData)
                s.store();

        }
        return  true;
    }


    /**
     * called before service is destroyed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("R",  "service done");
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

                SensorBasicData.deleteAll(SensorBasicData.class);
                for(int i=0;i< sites.length; i++){

                    if(saveBasicSiteData(sites[i])){

                        Message msg1 = new Message();
                        msg1.arg1=i;
                        handler.sendMessage(msg1);
                    }
                }

                // if the startTime is larger in seconds than interval to get history data
                if((System.currentTimeMillis()-startTime)/1000 >= HISTORY_UPDATE_SECONDDS_INTERVAL){
                    // we set to false so we can send a request
                    pulledHistory = false;
                    //
                }


                if(!pulledHistory) {

                    updatedSiteHistory[0]=updatedSiteHistory[1]=updatedSiteHistory[2]=updatedSiteHistory[3]=false;

                    SensorHistory.deleteAll(SensorHistory.class);
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
     * Handler to handle messages send by the thread to the service
     */
    class ServiceHandler extends Handler {

        @Override
        public void handleMessage(Message msg)
        {
            Log.i("service", msg.arg1+ " : message from thread");

            // process incoming messages here
            if(msg.arg1==-1){
                // stop the service
                getLooper().quit();
            }
            else if(msg.arg1==0 || msg.arg1==1 || msg.arg1==2 || msg.arg1==3) {
                // means tell fragments to update
                // tell Main ui to update
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(ACTION);
                broadcastIntent.putExtra("current", msg.arg1);
                sendBroadcast(broadcastIntent);
                LocalBroadcastManager.getInstance(BackgroundWorker.this).sendBroadcast(broadcastIntent);
            }
            else if(msg.arg1==10 || msg.arg1==11 || msg.arg1==12 || msg.arg1==13) {
                // means tell fragments to update
                // tell Main ui to update
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(ACTION);
                broadcastIntent.putExtra("history", msg.arg1-10);
                sendBroadcast(broadcastIntent);
                LocalBroadcastManager.getInstance(BackgroundWorker.this).sendBroadcast(broadcastIntent);
            }
        }
    }

}
