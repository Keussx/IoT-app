package com.ksopha.thanetearth.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class for formatting Dat received from Web server to human readable format
 * Created by Kelvin Sopha on 28/03/18.
 */

public class Formatter {

    private SimpleDateFormat oldFormat;
    private SimpleDateFormat newFormat;

    /**
     * Constructor
     */
    public Formatter(){
        oldFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.UK);
        newFormat = new SimpleDateFormat("dd/MM/yy--hh:mm a ", Locale.UK);
    }


    /**
     * formats the date string
     * @param date the string to format
     * @return formatted date
     */
    public String format(String date){
        try {
            Date d = oldFormat.parse(date);

            return newFormat.format(d);

        }catch (ParseException e){}

        return "";
    }

}
