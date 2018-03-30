package com.ksopha.thanetearth.ormObject;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by Kelvin Sopha on 29/03/18.
 */

public class Sensor extends SugarRecord{
    @Unique
    private String sensorID;
    private String name;
    private String site;

    public Sensor(String sensorID, String name, String site) {
        this.sensorID = sensorID;
        this.name = name;
        this.site = site;
    }

    public String getSensorID() {
        return sensorID;
    }

    public Sensor(){

    }

    public void setSensorID(String sensorID) {
        this.sensorID = sensorID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }
}
