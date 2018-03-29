package com.ksopha.thanetearth.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Kelvin Sopha on 28/03/18.
 */

public class Formatter {

    private SimpleDateFormat oldFormat;
    private SimpleDateFormat newFormat;

    public Formatter(){
        oldFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.UK);
        newFormat = new SimpleDateFormat("dd/MM/yy--hh:mm a ", Locale.UK);
    }

    public String format(String date){
        try {
            Date d = oldFormat.parse(date);

            return newFormat.format(d);

        }catch (ParseException e){}

        return "";
    }
}
