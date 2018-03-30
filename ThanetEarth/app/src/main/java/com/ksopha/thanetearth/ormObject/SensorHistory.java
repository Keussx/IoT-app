package com.ksopha.thanetearth.ormObject;

import com.orm.SugarRecord;

/**
 * Created by Kelvin Sopha on 27/03/18.
 */

public class SensorHistory extends SugarRecord {
    private Sensor sensor;
    private String type;
    private String date;
    private float value;

    public SensorHistory(Sensor sensor, String type, String date, float value) {
        this.sensor = sensor;
        this.type = type;
        this.date = date;
        this.value = value;
    }

    public SensorHistory() {
    }

    public void store(){
        save();
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
