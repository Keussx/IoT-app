package com.ksopha.thanetearth.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
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
import com.ksopha.thanetearth.R;
import com.ksopha.thanetearth.fragment.AlertsFragment;
import com.ksopha.thanetearth.fragment.BasicSiteFragment;
import com.ksopha.thanetearth.fragment.BatteryFragment;
import com.ksopha.thanetearth.fragment.MapFragment;
import com.ksopha.thanetearth.fragment.GreenhouseFragment;
import com.ksopha.thanetearth.fragment.StartupFragment;
import com.ksopha.thanetearth.service.BackgroundWorker;
import com.ksopha.thanetearth.service.NotificationHelper;

public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private final static String STARTUP_FRAG_TAG = "start_frag";
    private final static String MAPVIEW_FRAG_TAG = "map_frag";
    private final static String GRN_HOUSE1_TAG = "grn1_frag";
    private final static String GRN_HOUSE2_TAG = "grn2_frag";
    private final static String GRN_HOUSE3_TAG = "grn3_frag";
    private final static String OUT_FRAG = "outside_frag";
    private final static String ALERTS_FRAG = "alert_frag";
    private final static String BATT_FRAG = "power_frag";
    private final static int REQ_INTERVAL = 1000 * 60 ; // 1 minute in milliseconds
    private final static int ERROR_REQUEST_CODE = 8000; // random codes just to identify this activity
    private DrawerLayout navDrawer;
    private String currentFragment;
    private FragmentManager fragManager;
    private StartupFragment startupFragment;
    private MapFragment mapViewFragment;
    private GreenhouseFragment greenhouse1, greenhouse2, greenhouse3;
    private BasicSiteFragment outside;
    private BatteryFragment batteryFragment;
    private AlertsFragment alerts;
    private Intent backgroundServiceIntent;
    private IntentFilter intentFilter;
    private Runnable drawerClosedRun;
    private Handler navDrawerHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        navDrawerHandler = new Handler();

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
            changeToolBarTitle(GRN_HOUSE1_TAG);
        }
        else{
            // otherwise bundle contain states, so restore the states
            currentFragment = savedInstanceState.getString("currentFrag");
        }

        // set drawer listener
        navDrawer = (DrawerLayout) findViewById(R.id.nav_drawer);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, navDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            @Override
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();

                // If mPendingRunnable is not null, then add to the message queue
                if (drawerClosedRun != null) {
                    navDrawerHandler.post(drawerClosedRun);
                    drawerClosedRun = null;
                }
            }
        };

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
        greenhouse1 = (GreenhouseFragment) fragManager.findFragmentByTag(GRN_HOUSE1_TAG);
        greenhouse2 = (GreenhouseFragment) fragManager.findFragmentByTag(GRN_HOUSE2_TAG);
        greenhouse3 = (GreenhouseFragment) fragManager.findFragmentByTag(GRN_HOUSE3_TAG);
        outside = (BasicSiteFragment) fragManager.findFragmentByTag(OUT_FRAG);
        batteryFragment = (BatteryFragment) fragManager.findFragmentByTag(BATT_FRAG);
        alerts = (AlertsFragment) fragManager.findFragmentByTag(ALERTS_FRAG);

        // instantiate is null
        if(startupFragment == null){
            startupFragment = new StartupFragment();
        }
        if(mapViewFragment == null){
            mapViewFragment = new MapFragment();
        }
        if(greenhouse1 == null){
            greenhouse1 = GreenhouseFragment.newInstance("gh1");
        }
        if(greenhouse2 == null){
            greenhouse2 = GreenhouseFragment.newInstance("gh2");
        }
        if(greenhouse3 == null){
            greenhouse3 = GreenhouseFragment.newInstance("gh3");
        }
        if(outside == null){
            outside = BasicSiteFragment.newInstance("outside");
        }
        if(batteryFragment == null){
            batteryFragment = new BatteryFragment();
        }
        if(alerts == null){
            alerts = new AlertsFragment();
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
                fragmentTransaction.replace(R.id.fragment_holder, greenhouse1, GRN_HOUSE1_TAG);
                currentFragment = GRN_HOUSE1_TAG;
                break;

            case GRN_HOUSE2_TAG:
                fragmentTransaction.replace(R.id.fragment_holder, greenhouse2, GRN_HOUSE2_TAG);
                currentFragment = GRN_HOUSE2_TAG;
                break;

            case GRN_HOUSE3_TAG:
                fragmentTransaction.replace(R.id.fragment_holder, greenhouse3, GRN_HOUSE3_TAG);
                currentFragment = GRN_HOUSE3_TAG;
                break;

            case OUT_FRAG:
                fragmentTransaction.replace(R.id.fragment_holder, outside, OUT_FRAG);
                currentFragment = OUT_FRAG;
                break;

            case BATT_FRAG:
                fragmentTransaction.replace(R.id.fragment_holder, batteryFragment, BATT_FRAG);
                currentFragment = BATT_FRAG;
                break;

            case ALERTS_FRAG:
                fragmentTransaction.replace(R.id.fragment_holder, alerts, ALERTS_FRAG);
                currentFragment = ALERTS_FRAG;
                break;

            case MAPVIEW_FRAG_TAG:
                fragmentTransaction.replace(R.id.fragment_holder, mapViewFragment, MAPVIEW_FRAG_TAG);
                currentFragment = MAPVIEW_FRAG_TAG;

        }

        fragmentTransaction.commit();
    }



    private void changeToolBarTitle(String TAG){
        String title = (GRN_HOUSE1_TAG.equals(TAG)) ?
                "Greenhouse 1" : (GRN_HOUSE2_TAG.equals(TAG)) ?
                "Greenhouse 2" : (GRN_HOUSE3_TAG.equals(TAG)) ?
                "Greenhouse 3" : (OUT_FRAG.equals(TAG)) ?
                "Outdoor Monitoring Station" : (BATT_FRAG.equals(TAG)) ?
                "Sensor Power Levels" : (ALERTS_FRAG.equals(TAG)) ?
                "Alert Logs" : (MAPVIEW_FRAG_TAG.equals(TAG)) ?
                "Site Locations" : "";

        getSupportActionBar().setSubtitle(title);
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

        int id = item.getItemId();

        final String tag =  (id == R.id.grn1 && !currentFragment.equals(GRN_HOUSE1_TAG)) ?
            GRN_HOUSE1_TAG : (id == R.id.grn2 && !currentFragment.equals(GRN_HOUSE2_TAG)) ?
            GRN_HOUSE2_TAG : (id == R.id.grn3 && !currentFragment.equals(GRN_HOUSE3_TAG)) ?
            GRN_HOUSE3_TAG : (id == R.id.out && !currentFragment.equals(OUT_FRAG)) ?
            OUT_FRAG : (id == R.id.power && !currentFragment.equals(BATT_FRAG)) ?
            BATT_FRAG : (id == R.id.alerts && !currentFragment.equals(ALERTS_FRAG)) ?
            ALERTS_FRAG : (id == R.id.sites && !currentFragment.equals(MAPVIEW_FRAG_TAG)) ?
            MAPVIEW_FRAG_TAG : "";

        drawerClosedRun = new Runnable() {
            @Override
            public void run() {
                switchFragment(tag);
            }
        };


        changeToolBarTitle(tag);
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


    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);

        // when a new intent is received from notification
        if(intent.hasExtra("alerts")){
            Log.e("D", "has");
            switchFragment(ALERTS_FRAG);
        }
    }

    /**
     * BroadcastReceiver for listening to data that BackgroundWorker broadcasts asynchronously
     */
    private BroadcastReceiver serviceMsgReceiver = new  BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Greenhouse 1 current data
            if(intent.hasExtra("current")){

                Log.i("Main"," current data");

                int mode = intent.getIntExtra("current", -1);

                // update fragment data if it is visible
                if(mode==0 && greenhouse1.isVisible()){
                    greenhouse1.updateCurrentGreenhouseData();
                }
                else if (mode==1 && greenhouse2.isVisible()) {
                    greenhouse2.updateCurrentGreenhouseData();
                }
                else if (mode==2 && greenhouse3.isVisible()) {
                    greenhouse3.updateCurrentGreenhouseData();
                }
                else if (mode==3 && outside.isVisible()) {
                    outside.updateCurrentSiteData();
                }
                else if(mode==3 && batteryFragment.isVisible()){
                    batteryFragment.updateUiBatteryMeasures();
                }
                else if(mode==4 && alerts.isVisible()){
                    alerts.refreshLogViews();
                }

            }

            else if(intent.hasExtra("history")){

                Log.i("Main","Updating: history data");

                int mode = intent.getIntExtra("history", -1);

                // update greenhouse1 data if it is visible
                if(mode==0 && greenhouse1.isVisible()){
                    greenhouse1.updateHistoryData();
                }
                else if (mode==1 && greenhouse2.isVisible()) {
                    greenhouse2.updateHistoryData();
                }
                else if (mode==2 && greenhouse3.isVisible()) {
                    greenhouse3.updateHistoryData();
                }
                else if (mode==3 && outside.isVisible()) {
                    outside.updateHistoryData();
                }
            }

        }

    };


}
