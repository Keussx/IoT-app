package com.ksopha.thanetearth.weather;

import com.ksopha.thanetearth.R;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.TimeZone;

/**
 * Created by Kelvin Sopha on 24/03/18.
 */

public class WeatherJsonParser {

    public WeatherInfo getWeatherInfo(String json){

        if(json != null && json.length()>0) {

            // try parsing to objects
            try {
                //get the root json element
                JSONObject object = new JSONObject(json);

                JSONObject weather = object.getJSONArray("weather").getJSONObject(0);
                JSONObject main = object.getJSONObject("main");
                JSONObject sys = object.getJSONObject("sys");

                // find if it's night using sunrise and sunset UTC unix times in json
                boolean night  = isNight(sys.getInt("sunrise"), sys.getInt("sunset"));

                return new WeatherInfo(
                        conditionIdToDrawable(weather.getInt("id"), night),
                        weather.getString("description"),
                        main.getDouble("temp"),
                        main.getInt("pressure"),
                        main.getInt("humidity"),
                        main.getDouble("temp_min"),
                        main.getDouble("temp_max")
                );

            } catch (JSONException e) {

            }
        }
        return null;
    }

    private int conditionIdToDrawable(int conditionId, boolean night){
        // condition Id is mapped to an icon based on groups
        // reference: https://openweathermap.org/weather-conditions

        if(conditionId>=200 && conditionId<=232){
            // Group 2xx: Thunderstorm
            return R.drawable.weather_thunderstorm;
        }

        else if(conditionId>=300 && conditionId<=321){
            // Group 3xx: Drizzle
            return R.drawable.weather_drizzle;
        }

        else if(conditionId>=500 && conditionId<=531){
            // Group 5xx: Rain
            // light or moderate rain
            if(conditionId== 501 || conditionId==502){
                return night ? R.drawable.weather_rain_light_night : R.drawable.weather_rain_light_day;
            }
            return R.drawable.weather_rain_heavy;
        }

        else if(conditionId>=600 && conditionId<=622){
            // Group 6xx: Snow
            // snow with rain
            if(conditionId== 615 || conditionId==616){
                return R.drawable.weather_snow_rain;
            }
            return R.drawable.weather_snow;
        }

        else if(conditionId>=701 && conditionId<=781){
            // Group 7xx: Atmosphere
            return R.drawable.weather_atmosphere;
        }

        else if(conditionId==800){
            // Group 800: Clear
            return night ? R.drawable.weather_clear_sky_night : R.drawable.weather_clear_sky_day;
        }

        else if(conditionId>=801 && conditionId<=804){
            // Group 80x: Clouds
            return night ? R.drawable.weather_clouds_night : R.drawable.weather_clouds_day;
        }

        else if(conditionId>=900 && conditionId<=906){
            // Group 90x: Extreme
            switch (conditionId){
                case 900: return R.drawable.weather_extreme_tornado;
                case 901: return R.drawable.weather_extreme_storm;
                case 902: return R.drawable.weather_extreme_hurricane;
                case 903: return R.drawable.weather_extreme_cold;
                case 904: return R.drawable.weather_extreme_hot;
                case 905: return R.drawable.weather_extreme_windy;
                default: return R.drawable.weather_extreme_hail;
            }
        }
        else if(conditionId>=951 && conditionId<=962){
            // Group 9xx: Additional
            if(conditionId == 951){
                return R.drawable.weather_calm;
            }
            else if(conditionId>=952 && conditionId<=956){
                return R.drawable.weather_breeze;
            }
            else if(conditionId>=957 && conditionId<=959){
                return R.drawable.weather_gale;
            }
            return R.drawable.weather_extreme_storm;
        }
        else{
            // unknown icon
            return R.drawable.weather_unknown;
        }
    }


    private boolean isNight(int sunriseUTC, int sunsetUTC){

        // get current UTC time
        TimeZone london = TimeZone.getTimeZone("Europe/London");
        long currentUTC = System.currentTimeMillis();

        // offset time by London timezone, then convert to unix UTC time
        currentUTC += london.getOffset(currentUTC);
        currentUTC = currentUTC / 1000L;

        // if time is between sunrise and sunset, then it's not night
        if(currentUTC> sunriseUTC && currentUTC<sunsetUTC){
            return false;
        }
        return true;
    }
}
