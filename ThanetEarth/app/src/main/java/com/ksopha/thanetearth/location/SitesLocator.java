package com.ksopha.thanetearth.location;

import android.content.Context;
import android.os.AsyncTask;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ksopha.thanetearth.retrofit.RInterface;
import com.ksopha.thanetearth.retrofit.Site;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * This class performs location updates by finding location of a device and
 * others (based on json data received from a http request),
 * and updates a map
 * Created by Kelvin Sopha on 24/02/18.
 */

public class SitesLocator {

    private final static int UPDATE_INTERVAL_SECONDS = 10;
    private final static int FASTEST_INTERVAL_INTERVAL_SECONDS = 10;
    private final static int MAX_WAIT_TIME_SECONDS = 10;
    private ArrayList<MarkerOptions> markers;
    private WeakReference<Context> parent;
    private String sites_url;
    private SitesMarkerBuilder markerBuilder;
    private LatLngBounds.Builder mapBoundsBuilder;
    private GoogleMap map;
    private boolean busy;
    private boolean toMoveCamAgain;
    private Retrofit retrofit;
    private RInterface client;


    /**
     * Constructor
     * @param parent a Context reference
     */
    public SitesLocator(Context parent){

        this.sites_url = sites_url;
        this.parent = new WeakReference<>(parent);
        markerBuilder = new SitesMarkerBuilder();
        // a connect and read timeout of 5 seconds is used for webClient

        markers = new ArrayList<>();
        mapBoundsBuilder = new LatLngBounds.Builder();

        retrofit = new Retrofit.Builder()
                .baseUrl("http://shed.kent.ac.uk/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        client =  retrofit.create(RInterface.class);
    }


    /**
     * Start updates
     * @param map a map to save as reference
     * @param toMoveCamAgain if can set map camera position after map markers update
     */
    public void startPeriodicUpdates(GoogleMap map, boolean toMoveCamAgain){

        this.map = map;

        this.toMoveCamAgain = toMoveCamAgain;

        // execute asynctask to get locations
        new HTTPGetSitesTask(this).execute();
    }


    /**
     * To call if must stop location updates
     */
    public void stopPeriodicUpdates() {

    }



    /**
     * add markers to map and include in bounds builder
     * @param markers list of marker options representing markers
     */
    public void addMarkersAndSetBounds(List<MarkerOptions> markers){
        // add all markers
        for (MarkerOptions marker : markers) {
            if(marker != null) {
                // add marker to map
                map.addMarker(marker);

                // add to bounds builder
                mapBoundsBuilder.include(marker.getPosition());
            }
        }

    }


    /**
     * resets the Map camera so that all markers are visible
     */
    public void moveMapCamAboveMarkers(){
        // move camera using details from saved LatLngBounds
        if(map != null){
            try {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(mapBoundsBuilder.build(), 200));
            }catch(IllegalStateException e){

            }
        }

    }


    /**
     * returns all saved markers as marker options
     * @return saved markers
     */
    public ArrayList<MarkerOptions> getAllMarkerOptions(){
        return markers;
    }


    public Context getParent(){
        return parent.get();
    }


    /**
     * This class is an AsyncTask that retrieves locations via http.
     * Declared as static to prevent memory leaks. A weak reference to the parent class is used
     * It then saves locations as markers and updates map
     */
    private static class HTTPGetSitesTask extends AsyncTask<Void, Void, List<MarkerOptions>> {

        // weak reference to the parent activity
        private WeakReference<SitesLocator> parentReference;

        private HTTPGetSitesTask(SitesLocator updater){
            // create weak reference
            parentReference = new WeakReference<>(updater);
        }

        @Override
        protected List<MarkerOptions> doInBackground(Void... voids) {

            // get parent reference
            SitesLocator updater = parentReference.get();

            try {
                // run on background thread
                Response<List<Site>> sites = updater.client.sites().execute();

                return updater.markerBuilder.jsonToMarkerOptions(sites, parentReference.get());
            }catch(Exception e){
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<MarkerOptions> list) {
            // UI thread
            // get parent reference
            SitesLocator updater = parentReference.get();

            // save all markers
            updater.markers.addAll(list);

            //reset
            updater.map.clear();

            updater.addMarkersAndSetBounds(updater.markers);

            // on first update, change the map view so that all markers are visible
            // Do once, since user might be interacting with map afterwards
            if(updater.toMoveCamAgain) {
                updater.moveMapCamAboveMarkers();
                updater.toMoveCamAgain = false;
            }

            // reset busy state to false
            updater.busy = false;
        }
    }

}
