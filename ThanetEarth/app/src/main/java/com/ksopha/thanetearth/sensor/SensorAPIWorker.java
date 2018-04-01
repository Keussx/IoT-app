package com.ksopha.thanetearth.sensor;

import com.ksopha.thanetearth.http.HTTPClient;
import com.ksopha.thanetearth.ormObject.Sensor;
import com.ksopha.thanetearth.ormObject.SensorBasicData;
import com.ksopha.thanetearth.ormObject.SensorHistory;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for doing requests to the sensors API
 * Created by Kelvin Sopha on 25/03/18.
 */

public class SensorAPIWorker {

    private static final String BASE_URL="http://shed.kent.ac.uk/";
    private HTTPClient httpClient;
    private SensorJsonDataParser sensorJsonDataParser;


    /**
     * Constuctor
     */
    public SensorAPIWorker(){
        httpClient = new HTTPClient(5000,5000);
        sensorJsonDataParser = new SensorJsonDataParser();
    }


    /**
     * get basic current measures for all sensors from the API
     * @param siteId site Id to get details for
     * @param sensors list of sensors to get details for
     * @return list of sensor data
     */
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



    /**
     * returns a list of sensors from all sites
     * @return sensor list
     */
    public List<Sensor> getSensors(){

        // get device Ids here
        String response = httpClient.getHttpResponseAsString(BASE_URL + "sites");

        return sensorJsonDataParser.getSensorsFromJson(response);

    }



    /**
     * get basic current measures for a single sensors from the API
     * @param sensor sensor to get details for
     * @return sensor measurement details
     */
    private SensorBasicData getSensorBasicData(Sensor sensor){

        String deviceID = sensor.getId();

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


    /**
     * Get history data for a list of sensors, restrcited by site id and measurement type
     * @param url_extra url to get history data
     * @param type type of measurement
     * @param siteId id i=of site
     * @param sensors list of sensors to get history data
     * @return sensor data
     */
    public List<SensorHistory>  getGreenhouseHistoryData(String url_extra, String type, String siteId, List<Sensor> sensors){

        // will hold temp sensor history for each sensor
        List<SensorHistory> data = new ArrayList<>();

        // if the site we want to get all temp sensor history from exists
        if(sensors != null && sensors.get(0) != null){

            // for every sensors related to the site
            for(Sensor sensor: sensors){

                if(siteId.equals(sensor.getSite())){

                    String response = httpClient.getHttpResponseAsString(BASE_URL +
                            "device/" + sensor.getId() + url_extra);

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

