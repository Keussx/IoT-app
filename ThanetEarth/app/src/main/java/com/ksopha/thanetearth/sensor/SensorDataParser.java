package com.ksopha.thanetearth.sensor;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksopha.thanetearth.date.Formatter;
import com.ksopha.thanetearth.ormObject.Sensor;
import com.ksopha.thanetearth.ormObject.SensorBasicData;
import com.ksopha.thanetearth.ormObject.SensorHistory;
import com.ksopha.thanetearth.retrofit.History;
import com.ksopha.thanetearth.retrofit.SensorMeasure;
import com.ksopha.thanetearth.retrofit.Site;
import com.ksopha.thanetearth.retrofit.Zone;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;


/**
 * Class for parsing json sensor data
 * Created by Kelvin Sopha on 25/03/18.
 */

public class SensorDataParser {

    private ObjectMapper mapper;
    private Formatter formatter;


    /**
     * Constructor
     */
    public SensorDataParser(){
        mapper = new ObjectMapper();
        formatter = new Formatter();
    }


    /**
     * Get list of sensors from json
     * @return  sensor list
     */
    public List<Sensor> getSensorsFromJson(Response<List<Site>> sites){

        if(sites.isSuccessful()) {

            // try parsing to objects
            try {

                List<Sensor> sensors = new ArrayList<>();

                for(Site site: sites.body()){

                    List<Zone> zones = site.getZones();

                    for(Zone zone: zones) {
                        String deviceId = site.getId()+"_"+zone.getId();
                        sensors.add(new Sensor(deviceId, zone.getId(), site.getId()));
                    }
                }
                return sensors;

            } catch (Exception e) {Log.e("E", e.getMessage());
            }
        }
        return null;
    }


    /**
     * return sensor details using several json data
     * @return basic data instance
     */
    public SensorBasicData getSensorBasicData(Sensor sensor, Response<SensorMeasure> tds, Response<SensorMeasure> light,
                                              Response<SensorMeasure> temp, Response<SensorMeasure> moisture, Response<SensorMeasure> battery){


        if(sensor!=null && tds.isSuccessful() && light.isSuccessful() && temp.isSuccessful() &&
                moisture.isSuccessful() && battery.isSuccessful()) {

            // try parsing to objects
            try {
                return new SensorBasicData(
                        sensor,
                        tds.body().getValue(),
                        light.body().getValue(),
                        temp.body().getValue(),
                        moisture.body().getValue(),
                        battery.body().getValue()
                );



            } catch (Exception e) {
                Log.i("SensorDataParser", e.getMessage());
            }
        }
        return null;
    }


    /**
     * get list of sensor history records from json data
     * @param sensor sensor instance
     * @param type type of measurement
     * @return list of sensor history records
     */
    public List<SensorHistory> getSensorSensorHistoryListFromJson(Sensor sensor, String type, Response<List<History>> response ){

        List<SensorHistory> records = new ArrayList<>();

        // try parsing to objects
        try {

            for(History record: response.body()){

                records.add(new SensorHistory(sensor, type, formatter.format(record.getTime()),
                        record.getMean()));

            }

            return records;

        } catch (Exception e) {
            Log.i("SensorDataParser", e.getMessage());
        }

        return null;
    }


    private boolean jsonValid(String json){
        return json != null && json.length()>0;
    }

}


