package com.ksopha.thanetearth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ksopha.thanetearth.fragment.MapFragment;
import com.ksopha.thanetearth.fragment.GreenhouseFragment;
import com.ksopha.thanetearth.fragment.StartupFragment;
import com.ksopha.thanetearth.service.BackgroundWorker;

public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private final static String STARTUP_FRAG_TAG = "start_frag";
    private final static String MAPVIEW_FRAG_TAG = "map_frag";
    private final static String GRN_HOUSE1_TAG = "grn1_frag";
    private final static int REQ_INTERVAL = 1000 * 60 ; // 1 minute in milliseconds
    private final static int ERROR_REQUEST_CODE = 8000; // random codes just to identify this activity
    private DrawerLayout navDrawer;
    private String currentFragment;
    private FragmentManager fragManager;
    private StartupFragment startupFragment;
    private MapFragment mapViewFragment;
    private GreenhouseFragment greenhouse1Fragment;
    private Intent backgroundServiceIntent;
    private IntentFilter intentFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set toolbar as action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null){
            setSupportActionBar(toolbar);
        }

        // support status bar background for android versions 5.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }


        // get fragment manager and load fragments
        fragManager = getSupportFragmentManager();
        findOrCreateFragments();

        // when Main Activity starts for first time, show startup fragment
        if(savedInstanceState == null || !savedInstanceState.containsKey("currentFrag")){
            switchFragment(GRN_HOUSE1_TAG);
        }
        else{
            // otherwise bundle contain states, so restore the states
            currentFragment = savedInstanceState.getString("currentFrag");
        }

        // set drawer listener
        navDrawer = (DrawerLayout) findViewById(R.id.nav_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, navDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        navDrawer.addDrawerListener(toggle);
        toggle.syncState();

        // set navigation item selection listener
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(BackgroundWorker.ACTION);
        backgroundServiceIntent = new Intent(getApplicationContext(), BackgroundWorker.class);
        startBackgroundService();

    }



    /**
     * try to get fragment states back, if cannot, then instantiate
     */
    public void findOrCreateFragments(){
        // find fragments
        startupFragment = (StartupFragment) fragManager.findFragmentByTag(STARTUP_FRAG_TAG);
        mapViewFragment = (MapFragment) fragManager.findFragmentByTag(MAPVIEW_FRAG_TAG);
        greenhouse1Fragment = (GreenhouseFragment) fragManager.findFragmentByTag(GRN_HOUSE1_TAG);

        // instantiate is null
        if(startupFragment == null){
            startupFragment = new StartupFragment();
        }
        if(mapViewFragment == null){
            mapViewFragment = new MapFragment();
        }
        if(greenhouse1Fragment == null){
            greenhouse1Fragment = new GreenhouseFragment();
        }
    }



    /**
     * replaces a fragment visible on the screen by another
     * @param fragmentTag the tag of fragment to replace current visible fragment with
     */
    private void switchFragment(String fragmentTag){

        FragmentTransaction fragmentTransaction = fragManager.beginTransaction();

        switch (fragmentTag){
            case STARTUP_FRAG_TAG:
                fragmentTransaction.replace(R.id.fragment_holder, startupFragment, STARTUP_FRAG_TAG);
                currentFragment = STARTUP_FRAG_TAG;
                break;
            case GRN_HOUSE1_TAG:
                fragmentTransaction.replace(R.id.fragment_holder, greenhouse1Fragment, GRN_HOUSE1_TAG);
                currentFragment = GRN_HOUSE1_TAG;
                getSupportActionBar().setSubtitle("Greenhouse 1");
                break;
            case MAPVIEW_FRAG_TAG:
                fragmentTransaction.replace(R.id.fragment_holder, mapViewFragment, MAPVIEW_FRAG_TAG);
                currentFragment = MAPVIEW_FRAG_TAG;
                getSupportActionBar().setSubtitle(R.string.toolbar_title_map);

        }

        fragmentTransaction.commit();
    }



    /**
     * checks if Google Play services (for maps) is available & up to date on the device
     * @return true if services are ok, or else false
     */
    private boolean checkGooglePlayServices() {
        GoogleApiAvailability gapi = GoogleApiAvailability.getInstance();
        int result = gapi.isGooglePlayServicesAvailable(this);
        // if services available
        if (result == ConnectionResult.SUCCESS){
            // services are ok, return true
            return true;
        }
        //if there is an issue that can be resolved by the user,
        else if(gapi.isUserResolvableError(result)){
            // show error dialog to address issue, which will direct the user
            // to the device settings or play store
            gapi.getErrorDialog(this, result, ERROR_REQUEST_CODE).show();
        }
        //otherwise, device not supported
        else{
            //show simple error message
            Toast.makeText(this, "Google Services API unavailable", Toast.LENGTH_SHORT).show();
        }
        // return false services are unavailable
        return false;

    }



    /**
     * handles button click events (actually used by the getmap button of startup fragment ui)
     * @param v the view that generated the click event
     */
    public void onBtnClick(View v){

        // check if load_app button on was clicked
        if(v.getId() == R.id.load_app){

            if(!isOnline()){
                // show message to user if there is no internet
                Toast.makeText(this, "No Internet Connectivity", Toast.LENGTH_SHORT).show();
            }
            else {
                // show map (Main Activity) only if services are available
                if (checkGooglePlayServices()) {
                    // show map with markers of sites
                    switchFragment(MAPVIEW_FRAG_TAG);
                }
            }
        }

    }



    /**
     * check if there is internet connectivity (works on real device)
     * reference: https://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
     * According to https://proandroiddev.com/the-android-emulator-doesnt-suck-no-really-it-doesn-t-2e9e6a05a899,
     * the android AVD emulator cannot detect if there is no connectivity
     * @return true or false based on if there is a connection
     */
    private boolean isOnline(){
        ConnectivityManager conman =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;

        if (conman != null) {
            activeNetwork = conman.getActiveNetworkInfo();
        }

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    @Override
    public void onBackPressed() {

        // close drawer if opened
        if (navDrawer.isDrawerOpen(GravityCompat.START)) {
            navDrawer.closeDrawer(GravityCompat.START);
        }

        //if startup fragment is shown, then finish Activity
        if(currentFragment.equals(STARTUP_FRAG_TAG)){
            // tell Android system activity is done, and can be closed
            finish();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }




    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.grn1 && !currentFragment.equals(GRN_HOUSE1_TAG)) {
            switchFragment(GRN_HOUSE1_TAG);
        } else if (id == R.id.sites && !currentFragment.equals(MAPVIEW_FRAG_TAG)) {
            switchFragment(MAPVIEW_FRAG_TAG);
        }

        item.setChecked(true);

        navDrawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save tag of fragment currently visible
        outState.putString("currentFrag", currentFragment);
    }



    private void startBackgroundService(){
       startService(backgroundServiceIntent);
    }


    /**
     * Secret tree
     */
    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceMsgReceiver, intentFilter);
        super.onResume();

    }


    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceMsgReceiver);
        super.onPause();
    }


    /**
     * BroadcastReceiver for listening to data that BackgroundWorker broadcasts asynchronously
     */
    private BroadcastReceiver serviceMsgReceiver = new  BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Greenhouse 1 current data
            if(intent.hasExtra("current")){

                Log.i("Main","Updating: current data");

                // update greenhouse1 data if it is visible
                if(greenhouse1Fragment.isVisible()){
                    greenhouse1Fragment.updateCurrentGreenhouseData("gh1");
                }

            }

            else if(intent.hasExtra("temp_history")){

                Log.i("Main","Updating: history data");

                // update greenhouse1 data if it is visible
                if(greenhouse1Fragment.isVisible()){
                    greenhouse1Fragment.updateHistoryData("gh1");
                }

            }

        }

    };
}
