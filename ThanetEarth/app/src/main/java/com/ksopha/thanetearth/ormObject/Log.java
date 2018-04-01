package com.ksopha.thanetearth.ormObject;


import io.realm.RealmObject;

/**
 * Basic log message
 * Created by Kelvin Sopha on 31/03/18.
 */

public class Log extends RealmObject {
    private long date;
    private String msg;
    private boolean viewed;

    public Log(long date, String msg, boolean viewed) {
        this.date = date;
        this.msg = msg;
        this.viewed = viewed;
    }

    public Log(){

    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }
}
