package com.ksopha.thanetearth.location;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ksopha.thanetearth.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * This class handles the creation of MarkerOptions from json string containing location data
 * or using values (title, latitude, longitude) to represent a location.
 * Created by Kelvin Sopha on 23/02/18.
 */

public class SitesMarkerBuilder {

    private WeakReference<SitesLocator> sitesLocatorWeakReference;

    /**
     * Takes in a json string, parses it and returns list of MarkerOptions for several markers
     * @param json string to get MarkerOptions from
     * @return list of MarkerOptions
     */
    public List<MarkerOptions> jsonToMarkerOptions(String json, SitesLocator reference){

        sitesLocatorWeakReference = new WeakReference<SitesLocator>(reference);

        List<MarkerOptions> markers = new ArrayList<>();

        if(json != null && json.length()>0) {

            // try parsing to objects
            try {
                //get the root json element as array
                JSONArray jsonArray = new JSONArray(json);

                //iterate through the array, and get required data of each greenhouse
                for (int i = 0; i < jsonArray.length(); i++) {
                   JSONObject object = jsonArray.getJSONObject(i);

                   if(object.has("name") && object.has("lon") && object.has("lat"))
                   {
                       markers.add(
                               getMarkerOptions(object.getString("name"),
                                       object.getString("id"),
                                       Float.valueOf(object.getString("lat")),
                                       Float.valueOf(object.getString("lon"))));
                   }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return markers;
    }

    /**
     * Takes in values needed to construct a  MarkerOption, constructs and returns a MarkerOption
     * @param title the title of marker
     * @param lat the longitude
     * @param lon the longitude
     * @return the MarkerOption built
     */
    MarkerOptions getMarkerOptions(String title, String id, double lat, double lon){
        LatLng coordinate = new LatLng(lat, lon);

        return new MarkerOptions().title(title).position(coordinate)
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmap(id)));

    }

    Bitmap getMarkerBitmap(String id){
        BitmapDrawable bitmapdr;

        Context parent = sitesLocatorWeakReference.get().getParent();

        switch (id){
            case "gh1":
                bitmapdr = (BitmapDrawable) parent.getResources().getDrawable(R.drawable.marker_gh1);
                break;
            case "gh2":
                bitmapdr = (BitmapDrawable) parent.getResources().getDrawable(R.drawable.marker_gh2);
                break;
            case "gh3":
                bitmapdr = (BitmapDrawable) parent.getResources().getDrawable(R.drawable.marker_gh3);
                break;
            default:
                bitmapdr = (BitmapDrawable) parent.getResources().getDrawable(R.drawable.marker_outside);
        }

        return Bitmap.createScaledBitmap(bitmapdr.getBitmap(), 150, 150, false);
    }
}
