package com.ksopha.thanetearth.service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.ksopha.thanetearth.ob.Sensor;
import com.ksopha.thanetearth.ob.SensorBasicData;
import com.ksopha.thanetearth.sensor.SensorAPIWorker;
import com.ksopha.thanetearth.ob.SensorHistory;
import java.lang.ref.WeakReference;
import java.util.List;


/**
 * Created by Kelvin Sopha on 25/03/18.
 */

public class BackgroundWorker extends Service {

    public static final String ACTION = "com.ksopha.thanetearth.service.BackgroundWorker";
    private SensorAPIWorker sensorAPIWorker;
    private List<Sensor> sensors;
    private ServiceHandler serviceHandler;
    private LooperThread thread;
    private boolean pulledHistory;
    private static final int SECONDDS_INTERVAL = 20; //debug


    @Override
    public void onCreate() {
        super.onCreate();
        sensorAPIWorker = new SensorAPIWorker();

        serviceHandler = new ServiceHandler();

        thread = new LooperThread(this, serviceHandler);
        thread.start();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // run work on background thread
        Log.i("EE","pulling current sensor measurements");

        if(!thread.isAlive()){
            thread.start();
        }

        return START_STICKY;

    }

    private Intent getIntent(){
        // create Intent
        Intent intent = new Intent(ACTION);
        intent.putExtra("resultCode", Activity.RESULT_OK);
        return  intent;
    }


    private void getSiteIdZones(){
        if(sensors==null || sensors.isEmpty()) {
            // clear since data might change on server
            Sensor.deleteAll(Sensor.class);
            sensors = sensorAPIWorker.getSaveAndReturnSensors();
        }
    }

    private void saveBasicSiteData(){

        List<SensorBasicData> siteData = sensorAPIWorker.getGreenHouseBasicData("gh1", sensors);

        //empty old vals
        SensorBasicData.deleteAll(SensorBasicData.class);
        // save to database
        for(SensorBasicData s:siteData)
            s.store();

    }



    private void saveGreenhouseTempHistory(){

        List<SensorHistory> siteData = sensorAPIWorker.getGreenhouseHistoryData("/temperature/hour", "temperature", "gh1" , sensors);

        if(siteData==null || siteData.size()==0){
            pulledHistory= false;
            return;
        }

        //empty old values
        SensorHistory.deleteAll(SensorHistory.class);
        // save to database
        for(SensorHistory s:siteData)
            s.store();

    }


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

            Log.i("EE","thread running");

            while(isRunning){

                getSiteIdZones();

                saveBasicSiteData();
                Message msg1 = new Message();
                msg1.arg1=1;
                handler.sendMessage(msg1);

                if(!pulledHistory) {
                    pulledHistory=true;
                    saveGreenhouseTempHistory();
                    Message msg2 = new Message();
                    msg2.arg1=2;
                    handler.sendMessage(msg2);
                }


                try {
                    Thread.sleep(1000 * SECONDDS_INTERVAL);
                }catch(Exception e){}
            }

            // start a loop that listens for msg
            Looper.loop();
        }
    }



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
            else if(msg.arg1==1) {
                // means tell fragments to update
                // tell Main ui to update
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(ACTION);
                broadcastIntent.putExtra("current", 1);
                sendBroadcast(broadcastIntent);
                LocalBroadcastManager.getInstance(BackgroundWorker.this).sendBroadcast(broadcastIntent);
            }
            else if(msg.arg1==2) {
                // means tell fragments to update
                // tell Main ui to update
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(ACTION);
                broadcastIntent.putExtra("temp_history", 1);
                sendBroadcast(broadcastIntent);
                LocalBroadcastManager.getInstance(BackgroundWorker.this).sendBroadcast(broadcastIntent);
            }
        }
    }

}
