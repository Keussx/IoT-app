package com.ksopha.thanetearth.ormObject;

import io.realm.RealmObject;

/**
 * Created by Kelvin Sopha on 27/03/18.
 */

public class SensorHistory extends RealmObject {
    private Sensor sensor;
    private String type;
    private long date;
    private float value;

    public SensorHistory(Sensor sensor, String type, long date, float value) {
        this.sensor = sensor;
        this.type = type;
        this.date = date;
        this.value = value;
    }

    public SensorHistory(){

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

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
