package com.ksopha.thanetearth.ormObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Kelvin Sopha on 29/03/18.
 */

public class Sensor extends RealmObject {
    @PrimaryKey
    private String id;
    private String name;
    private String site;

    public Sensor(String sensorID, String name, String site) {
        this.id = sensorID;
        this.name = name;
        this.site = site;
    }

    public Sensor(){

    }

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
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
