package com.ksopha.thanetearth.ob;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by Kelvin Sopha on 29/03/18.
 */

public class Sensor extends SugarRecord{
    @Unique
    private String sensorID;
    private String site;

    public Sensor(String sensorID, String site) {
        this.sensorID = sensorID;
        this.site = site;
    }

    public Sensor() {
    }

    public String getSensorID() {
        return sensorID;
    }

    public void setSensorID(String sensorID) {
        this.sensorID = sensorID;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }
}
