package com.ksopha.thanetearth.checker;

/**
 * Class checks measurements
 * Created by Kelvin Sopha on 29/03/18.
 */

public class MeasurementChecker {

    /**
     * Get a string if val is lower than min/higher than max, or null or not both
     * @param min min value
     * @param max max value
     * @param val value to check
     * @return compared state
     */
    private String getHigherLowerString(double min, double max, double val){

        if (val <min || val >max){
            return val <min ? "low" : "high";
        }
        return null;
    }



    /**
     * Method finds if a sensor temperature value is ok for a site
     * @param siteIndex site sensor belongs to
     * @param val the temperature
     * @return state
     */
    public String  checkTempAndGetString(int siteIndex, int val){

        switch (siteIndex){
            case 0 :
                return getHigherLowerString(18,35, val);
            case 1 :
                return getHigherLowerString(18,27, val);
            case 2 :
                return getHigherLowerString(15,32, val);
        }
        return null;
    }


    /**
     * Method finds if a sensor soil moisture value is ok for a site
     * @param siteIndex site sensor belongs to
     * @param val the moisture % value
     * @return state
     */
    public String  checkSoilMoistureAndGetString(int siteIndex, int val){

        switch (siteIndex){
            case 0 :
                return getHigherLowerString(50,75, val);
            case 1 :
                return getHigherLowerString(30,45, val);
            case 2 :
                return getHigherLowerString(40,55, val);
        }
        return null;
    }




    /**
     * Method finds if a sensor Soil Nutrient value is ok for a site
     * @param siteIndex site sensor belongs to
     * @param val the TDS value
     * @return state
     */
    public String  checkSoilNutrientAndGetString(int siteIndex, int val){

        switch (siteIndex){
            case 0 :
                return getHigherLowerString(300,700, val);
            case 1 :
                return getHigherLowerString(280,700, val);
        }
        return null;
    }


    /**
     * Method finds if a sensor light intensity value is ok for a site
     * @param siteIndex site sensor belongs to
     * @param val the lux value
     * @return state
     */
    public String  checkLuxAndGetString(int siteIndex, int val){

        switch (siteIndex){
            case 0 :
                return getHigherLowerString(10000,50000, val);
            case 1 :
                return getHigherLowerString(10000,50000, val);
            case 2 :
                return getHigherLowerString(10000,50000, val);
        }
        return null;
    }

    /**
     * Method finds if a sensor battery value is ok for a site
     * @param val the battery %
     * @return state
     */
    public String  checkBatteryAndGetString(int val){

        if(val <15)
            return "critical";
        return null;
    }
}
