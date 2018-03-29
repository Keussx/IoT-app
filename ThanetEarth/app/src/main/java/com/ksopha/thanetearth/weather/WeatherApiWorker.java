package com.ksopha.thanetearth.weather;

import android.os.AsyncTask;
import com.ksopha.thanetearth.fragment.GreenhouseFragment;
import com.ksopha.thanetearth.http.HTTPClient;
import java.lang.ref.WeakReference;


public class WeatherApiWorker {

    private static final String OWEATHER_API_KEY = "1b869259a574995f127d33946d71fc5d";
    private static final String BASE_URL="http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s";
    private static final String CITY_COUNTRY = "Kent,Uk";
    private HTTPClient httpClient;
    private WeakReference<GreenhouseFragment> dashboard;
    private WeatherJsonParser weatherJsonParser;


    public WeatherApiWorker(GreenhouseFragment reference){
        dashboard = new WeakReference<GreenhouseFragment>(reference);
        httpClient = new HTTPClient(5000,5000);
        weatherJsonParser = new WeatherJsonParser();
    }


    public void getTodaysWeather(){
        new HTTPGetTodaysWeatherTask(this).execute();
    }


    /**
     * This class is an AsyncTask that retrieves locations via http.
     * Declared as static to prevent memory leaks. A weak reference to the parent class is used
     * It then saves locations as markers and updates map
     */
    private static class HTTPGetTodaysWeatherTask extends AsyncTask<Void, Void, WeatherInfo> {

        // weak reference to the parent activity
        private WeakReference<WeatherApiWorker> parent;

        private HTTPGetTodaysWeatherTask(WeatherApiWorker reference){
            // create weak reference
            parent = new WeakReference<>(reference);
        }

        @Override
        protected WeatherInfo doInBackground(Void... voids) {

            // get  today's weather data
            String request = String.format(BASE_URL, CITY_COUNTRY, OWEATHER_API_KEY);
            String response = parent.get().httpClient.getHttpResponseAsString(request);

            // parse json to create WeatherInfo object
            return parent.get().weatherJsonParser.getWeatherInfo(response);
        }

        @Override
        protected void onPostExecute(WeatherInfo weatherInfo) {
            // update dashboard using UI thread
            if(parent.get().dashboard != null)
                parent.get().dashboard.get().updateWeatherToday(weatherInfo);
        }
    }

}
