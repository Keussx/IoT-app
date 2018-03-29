package com.ksopha.thanetearth.sensor;

import android.util.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksopha.thanetearth.date.Formatter;
import com.ksopha.thanetearth.ob.Sensor;
import com.ksopha.thanetearth.ob.SensorBasicData;
import com.ksopha.thanetearth.ob.SensorHistory;
import com.orm.SugarRecord;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Kelvin Sopha on 25/03/18.
 */

public class SensorJsonDataParser {

    private ObjectMapper mapper;
    private Formatter formatter;

    public SensorJsonDataParser(){
        mapper = new ObjectMapper();
        formatter = new Formatter();
    }


    public List<Sensor> getSensorsFromJson(String json){

        if(json != null && json.length()>0) {

            // try parsing to objects
            try {

                List<Sensor> sensors = new ArrayList<>();

                //get the root json element
                JsonNode array = mapper.readTree(json);

                if(!array.isArray())
                    throw new ParseException("Wrong data",-1);

                for(JsonNode site: array){

                    JsonNode zones = site.get("zones");

                    String siteId = site.get("id").asText();

                    for(JsonNode zone: zones) {
                        String deviceId = site.get("id").asText()+"_"+zone.get("id").asText();
                        sensors.add(new Sensor(deviceId, siteId));
                    }
                }

                if(sensors!=null && sensors.size()>0){
                    //save and return
                    SugarRecord.saveInTx(sensors);
                    return sensors;
                }



            } catch (Exception e) {Log.e("E", e.getMessage());
            }
        }
        return null;
    }




    public SensorBasicData getSensorBasicData(Sensor sensor, String tdsJson, String lightJson,
                                              String tempJson, String moistureJson, String batteryJson){


        if(sensor!=null && jsonValid(tdsJson) && jsonValid(lightJson) && jsonValid(tempJson)
                && jsonValid(moistureJson) && jsonValid(batteryJson)) {

            // try parsing to objects
            try {
                //get the root json element
                JsonNode tds = mapper.readTree(tdsJson);
                JsonNode light = mapper.readTree(lightJson);
                JsonNode temp = mapper.readTree(tempJson);
                JsonNode moisture = mapper.readTree(moistureJson);
                JsonNode battery = mapper.readTree(batteryJson);

                return new SensorBasicData(
                        sensor,
                        tds.get("value").asInt(),
                        light.get("value").asInt(),
                        temp.get("value").asInt(),
                        moisture.get("value").asInt(),
                        battery.get("value").asInt()
                );



            } catch (Exception e) {
                Log.i("SensorJsonDataParser", e.getMessage());
            }
        }
        return null;
    }


    public List<SensorHistory> getSensorSensorHistoryListFromJson(Sensor sensor, String type, String json){

        List<SensorHistory> records = new ArrayList<>();

        // try parsing to objects
        try {
            //get the root json element
            JsonNode array = mapper.readTree(json);

            for(JsonNode record: array){

                records.add(new SensorHistory(sensor, type, formatter.format(record.get("time").asText()),
                        (float)record.get("mean").asDouble()));

            }

            return records;

        } catch (Exception e) {
            Log.i("SensorJsonDataParser", e.getMessage());
        }

        return null;
    }


    private boolean jsonValid(String json){
        return json != null && json.length()>0;
    }

}


