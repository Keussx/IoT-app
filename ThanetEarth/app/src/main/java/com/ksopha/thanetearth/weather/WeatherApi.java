package com.ksopha.thanetearth.weather;

import com.ksopha.thanetearth.checker.MeasurementChecker;
import com.ksopha.thanetearth.ormObject.Log;
import com.ksopha.thanetearth.retrofit.Forecast;
import com.ksopha.thanetearth.retrofit.RInterface;
import com.ksopha.thanetearth.retrofit.WeatherData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by Kelvin Sopha on 02/04/18.
 */

public class WeatherApi {
    private static final String BASE_URL="https://query.yahooapis.com/v1/public/";
    private static final String CITY_COUNTRY = "Kent, GB";
    private Retrofit retrofit;
    private RInterface client;
    private MeasurementChecker measurementChecker;

    public WeatherApi(){
        measurementChecker = new MeasurementChecker();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        client =  retrofit.create(RInterface.class);
    }


    public void getAlerts(){

        try {
            Response<WeatherData> forecasts = client.getWeatherForecast().execute();

            List<Forecast> lists = forecasts.body().getQuery().getResults().getChannel().getItem().getForecast();

            List<Forecast> greenhouse1highTemperatures = new ArrayList<>();
            List<Forecast> greenhouse2highTemperatures = new ArrayList<>();
            List<Forecast> greenhouse3highTemperatures = new ArrayList<>();
            List<Forecast> greenhouse1lowTemperatures = new ArrayList<>();
            List<Forecast> greenhouse2lowTemperatures = new ArrayList<>();
            List<Forecast> greenhouse3lowTemperatures = new ArrayList<>();
            List<Log> logs = new ArrayList<>();

            for(Forecast main: lists){

                if(measurementChecker.checkTempAndGetString(0, Integer.parseInt(main.getHigh())).equals("high")){
                    greenhouse1highTemperatures.add(main);
                }
                if(measurementChecker.checkTempAndGetString(1, Integer.parseInt(main.getHigh())).equals("high")){
                    greenhouse2highTemperatures.add(main);
                }
                if(measurementChecker.checkTempAndGetString(2, Integer.parseInt(main.getHigh())).equals("high")){
                    greenhouse2highTemperatures.add(main);
                }
                if(measurementChecker.checkTempAndGetString(0, Integer.parseInt(main.getLow())).equals("low")){
                    greenhouse1lowTemperatures.add(main);
                }
                if(measurementChecker.checkTempAndGetString(1, Integer.parseInt(main.getLow())).equals("low")){
                    greenhouse2lowTemperatures.add(main);
                }
                if(measurementChecker.checkTempAndGetString(2, Integer.parseInt(main.getLow())).equals("low")){
                    greenhouse3lowTemperatures.add(main);
                }

            }

            android.util.Log.e("R", greenhouse3lowTemperatures.size()+"");



        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
