package com.ksopha.thanetearth.ormObject;

import com.orm.SugarRecord;

/**
 * Created by Kelvin Sopha on 29/03/18.
 */

public class SensorBasicData extends SugarRecord {
    private Sensor sensor;
    private int tds;
    private int light;
    private int temperature;
    private int moisture;
    private int batteryLevel;

    public SensorBasicData(Sensor sensor, int tds, int light, int temperature, int moisture, int batteryLevel) {
        this.sensor = sensor;
        this.tds = tds;
        this.light = light;
        this.temperature = temperature;
        this.moisture = moisture;
        this.batteryLevel = batteryLevel;
    }

    public SensorBasicData() {

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

    public int getTds() {
        return tds;
    }

    public void setTds(int tds) {
        this.tds = tds;
    }

    public int getLight() {
        return light;
    }

    public void setLight(int light) {
        this.light = light;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getMoisture() {
        return moisture;
    }

    public void setMoisture(int moisture) {
        this.moisture = moisture;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
}
