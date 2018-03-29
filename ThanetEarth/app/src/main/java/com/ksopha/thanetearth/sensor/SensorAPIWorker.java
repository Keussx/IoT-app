package com.ksopha.thanetearth.sensor;

import com.ksopha.thanetearth.http.HTTPClient;
import com.ksopha.thanetearth.ob.Sensor;
import com.ksopha.thanetearth.ob.SensorBasicData;
import com.ksopha.thanetearth.ob.SensorHistory;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kelvin Sopha on 25/03/18.
 */

public class SensorAPIWorker {

    private static final String BASE_URL="http://shed.kent.ac.uk/";
    private HTTPClient httpClient;
    private SensorJsonDataParser sensorJsonDataParser;


    public SensorAPIWorker(){
        httpClient = new HTTPClient(5000,5000);
        sensorJsonDataParser = new SensorJsonDataParser();
    }



    public List<SensorBasicData> getGreenHouseBasicData(String siteId, List<Sensor> sensors){

        List<SensorBasicData> data = new ArrayList<>();

        if(sensors != null){

            // for every site
            for(Sensor sensor: sensors){

                if(siteId.equals(sensor.getSite())){

                    SensorBasicData basicData = getSensorBasicData(sensor);

                    // add basic data if ok
                    if(basicData != null)
                        data.add(basicData);

                }

            }

        }

        return data;
    }


    public List<Sensor> getSaveAndReturnSensors(){

        // get device Ids here
        String response = httpClient.getHttpResponseAsString(BASE_URL + "sites");

        return sensorJsonDataParser.getSensorsFromJson(response);

    }




    private SensorBasicData getSensorBasicData(Sensor sensor){

        String deviceID = sensor.getSensorID();

        // get all device data here

        String tdsResponse = httpClient.getHttpResponseAsString(BASE_URL +
                "device/" + deviceID + "/tds");

        String lightResponse = httpClient.getHttpResponseAsString(BASE_URL +
                "device/" + deviceID + "/light");

        String tempResponse = httpClient.getHttpResponseAsString(BASE_URL +
                "device/" + deviceID + "/temperature");

        String moistureResponse = httpClient.getHttpResponseAsString(BASE_URL +
                "device/" + deviceID + "/moisture");

        String batteryResponse = httpClient.getHttpResponseAsString(BASE_URL +
                "device/" + deviceID + "/battery");

        return sensorJsonDataParser.getSensorBasicData(sensor, tdsResponse,
                lightResponse, tempResponse, moistureResponse, batteryResponse);
    }


    public List<SensorHistory>  getGreenhouseHistoryData(String url_extra, String type, String siteId, List<Sensor> sensors){

        // will hold temp sensor history for each sensor
        List<SensorHistory> data = new ArrayList<>();

        // if the site we want to get all temp sensor history from exists
        if(sensors != null && sensors.get(0) != null){

            // for every sensors related to the site
            for(Sensor sensor: sensors){

                if(siteId.equals(sensor.getSite())){

                    String response = httpClient.getHttpResponseAsString(BASE_URL +
                            "device/" + sensor.getSensorID() + url_extra);

                    List<SensorHistory> entries =
                            sensorJsonDataParser.getSensorSensorHistoryListFromJson(sensor, type, response);

                    if(entries!=null && entries.size()>0)
                        data.addAll(entries);
                }

            }

        }

        return data;
    }
}

