package com.ksopha.thanetearth.retrofit;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Kelvin Sopha on 02/04/18.
 */

public interface RInterface {

    @GET("sites")
    Call<List<Site>> sites();

    @GET("device/{device}/{measure}")
    Call<SensorMeasure> sensorMeasure(@Path("device") String device, @Path("measure") String measure);

    @GET("device/{device}/{measure}/hour")
    Call<List<History>> sensorMeasureHistory(@Path("device") String device, @Path("measure") String measure);


    @GET("yql?q=select * from weather.forecast where woeid in (select woeid from geo.places(1) where text='Kent, GB') and u='c' &format=json ")
    Call<WeatherData> getWeatherForecast();

}
