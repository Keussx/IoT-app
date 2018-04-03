package com.ksopha.thanetearth.sensor;

import com.ksopha.thanetearth.ormObject.Sensor;
import com.ksopha.thanetearth.ormObject.SensorBasicData;
import com.ksopha.thanetearth.ormObject.SensorHistory;
import com.ksopha.thanetearth.retrofit.History;
import com.ksopha.thanetearth.retrofit.SensorMeasure;
import com.ksopha.thanetearth.retrofit.RInterface;
import com.ksopha.thanetearth.retrofit.Site;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;


/**
 * Class for doing requests to the sensors API
 * Created by Kelvin Sopha on 25/03/18.
 */

public class SensorAPIWorker {

    private static final String BASE_URL="http://shed.kent.ac.uk/";
    private SensorDataParser sensorDataParser;
    private Retrofit retrofit;
    private RInterface client;


    /**
     * Constuctor
     */
    public SensorAPIWorker(){
        sensorDataParser = new SensorDataParser();
        retrofit = new Retrofit.Builder()
                .baseUrl("http://shed.kent.ac.uk/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        client =  retrofit.create(RInterface.class);
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
        Response<List<Site>> sites = null;
        try {
            sites = client.sites().execute();
            return sensorDataParser.getSensorsFromJson(sites);
        } catch (IOException e) {
            return null;
        }
    }



    /**
     * get basic current measures for a single sensors from the API
     * @param sensor sensor to get details for
     * @return sensor measurement details
     */
    private SensorBasicData getSensorBasicData(Sensor sensor){

        String deviceID = sensor.getId();

        // get all device data here
        try {

            Response<SensorMeasure> tds = client.sensorMeasure(deviceID, "tds").execute();

            Response<SensorMeasure> light = client.sensorMeasure(deviceID, "light").execute();

            Response<SensorMeasure> temp = client.sensorMeasure(deviceID, "temperature").execute();

            Response<SensorMeasure> moisture = client.sensorMeasure(deviceID, "moisture").execute();

            Response<SensorMeasure> battery = client.sensorMeasure(deviceID, "battery").execute();

            return sensorDataParser.getSensorBasicData(sensor, tds, light, temp, moisture, battery);

        }catch(Exception e){
            return null;
        }
    }


    /**
     * Get history data for a list of sensors, restrcited by site id and measurement type
     * @param type type of measurement
     * @param siteId id i=of site
     * @param sensors list of sensors to get history data
     * @return sensor data
     */
    public List<SensorHistory>  getGreenhouseHistoryData(String type, String siteId, List<Sensor> sensors){

        // will hold temp sensor history for each sensor
        List<SensorHistory> data = new ArrayList<>();

        // if the site we want to get all temp sensor history from exists
        if(sensors != null && sensors.get(0) != null){

            // for every sensors related to the site
            for(Sensor sensor: sensors){

                if(siteId.equals(sensor.getSite())){

                    try {
                        Response<List<History>> response = client.sensorMeasureHistory(sensor.getId(), type).execute();

                        List<SensorHistory> entries =
                                sensorDataParser.getSensorSensorHistoryListFromJson(sensor, type, response);

                        if (entries != null && entries.size() > 0)
                            data.addAll(entries);
                    }catch(Exception t){
                    }
                }

            }

        }

        return data;
    }
}

