package com.ksopha.thanetearth.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ksopha.thanetearth.R;
import com.ksopha.thanetearth.location.SitesLocator;
import java.util.List;

/**
 * Class for fragment with map
 * Created by Kelvin Sopha on 22/03/18.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private final String MAP_BUNDLE_KEY = "MapV";
    private SitesLocator locationUpdater;
    private MapView mapView;
    private List<MarkerOptions> savedMarkerStates;
    private boolean doingLocationUpdates;
    private boolean toMoveCamAgain;


    /**
     * called at creation of fragment
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // prevent fragment from being re-created when parent Activity is destroyed
        setRetainInstance(true);
    }


    /**
     * called to instantiate fragment ui
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_map, container, false);

        mapView =  root.findViewById(R.id.mapView);

        // check for saved mapView state
        Bundle mapViewBundle = null;

        // if app just started
        if(savedInstanceState == null){
            // if view was re-created then must reset map position to show all markers
            // on first time viewing of map
            toMoveCamAgain = true;

        }
        else{
            // get map bundle
            mapViewBundle = savedInstanceState.getBundle(MAP_BUNDLE_KEY);

            // try restore doingUpdates state
            if(savedInstanceState.containsKey("doingUpdates")){
                doingLocationUpdates = savedInstanceState.getBoolean("doingUpdates");
            }

            // try restore toMoveCamAgain state
            if (savedInstanceState.containsKey("toMoveCamAgain")){
                toMoveCamAgain = savedInstanceState.getBoolean("toMoveCamAgain");
            }

            // try restore saved_map_markers state
            if (savedInstanceState.containsKey("saved_map_markers")){
                savedMarkerStates = savedInstanceState.getParcelableArrayList("saved_map_markers");
            }
        }

        // create and load map
        mapView.onCreate(mapViewBundle);

        if(locationUpdater == null){
            locationUpdater = new SitesLocator(getActivity());
        }

        return root;
    }



    /**
     * called when map ui ready
     */
    @Override
    public void onMapReady(GoogleMap map) {

        // set type
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        map.getUiSettings().setZoomControlsEnabled(true);


        //restore state markers first
        if(savedMarkerStates != null && savedMarkerStates.size()>0){
            map.clear();
            for(MarkerOptions m: savedMarkerStates)
                map.addMarker(m);
        }

        if (!doingLocationUpdates) {
            doingLocationUpdates = true;

            //start location updates on the map
            locationUpdater.startPeriodicUpdates(map, toMoveCamAgain);
            toMoveCamAgain = false;
        }
    }


    /**
     * resets the map view location visible to the user
     */
    public void resetMapViewLocation(){

        if(locationUpdater != null){
            locationUpdater.moveMapCamAboveMarkers();
        }
    }



    /**
     * called when fragment id made visible to user
     */
    @Override
    public void onResume() {

        super.onResume();
        // invoke onResume of mapView
        if (mapView != null) {
            mapView.onResume();
        }

        // load map asynchronously
        if(mapView != null)
            mapView.getMapAsync(this);

    }


    /**
     * called when fragment goes off screen
     */
    @Override
    public void onPause() {
        //invoke onPause of mapView
        if (mapView != null) {
            mapView.onPause();
        }

        // stop regular updates
        doingLocationUpdates = false;
        locationUpdater.stopPeriodicUpdates();

        super.onPause();
    }



    /**
     * called when fragment needs to save states
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save doing updates state
        outState.putBoolean("doingUpdates", doingLocationUpdates);

        // save if to move map cam again
        outState.putBoolean("toMoveCamAgain", toMoveCamAgain);

        // save last used map markers
        if(locationUpdater != null){
            outState.putParcelableArrayList("saved_map_markers",locationUpdater.getAllMarkerOptions());
        }

        Bundle mapBundle = outState.getBundle(MAP_BUNDLE_KEY);

        if (mapBundle == null) {
            mapBundle = new Bundle();
            outState.putBundle(MAP_BUNDLE_KEY, mapBundle);
        }

        // invoke on MapView
        mapView.onSaveInstanceState(mapBundle);
    }



    /**
     * called before fragment is destroyed
     */
    @Override
    public void onDestroy() {
        if (mapView != null) {
            // invoke destroy on mapView for cleanup
            mapView.onDestroy();
        }
        super.onDestroy();
    }

}
