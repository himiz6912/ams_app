package jp.hmproject.ams_app;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by hm on 12/25/2016.
 */
public class AMS_LocationManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener {
/*
    final String TAG = "AMS_LocationManager";
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;
    private Location location;
    private boolean update_flg;

    public AMS_LocationManager(Context ctx) {
        context = ctx;
        update_flg = false;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG,"onConnectionFailed:" + connectionResult.getErrorMessage());
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try{
            if(location == null){
                location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
        }catch (SecurityException e){
            Log.e(TAG,"onConnected:" + e.getMessage());
        }
        if(update_flg)startService();
    }
    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }
    @Override
    public void onLocationChanged(Location loc) {
        location = loc;
    }

    public void finish(){
        mGoogleApiClient.disconnect();
    }
    public void initialize(LocationRequest lr){
        locationRequest = lr;
    }
    private void startService(){
        try {
//            Location currentLocation = LocationServices.FusedLocationApi
//                    .getLastLocation(mGoogleApiClient);
//            if(currentLocation != null){
//                location = currentLocation;
//            }else{
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient,locationRequest,this);
//            }
        }catch(SecurityException e){
            Log.e(TAG,"startService:" + e.getMessage());
        }
    }
    public void stopService(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }
    public Location getLocationData(){
        return location;
    }
}*/

    protected static final String TAG = "AMS_LocationManager";
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;

    protected Boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;

    public AMS_LocationManager(Context context,int interval) {
        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest(interval);
        mGoogleApiClient.connect();
    }
    protected void createLocationRequest(int i) {
        long interval = i > 1? (i - 1) * 1000: i * 1000;
        long fastest_interval = interval / 2;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(fastest_interval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }catch (SecurityException e){
            Log.e(TAG,"startLocation:" + e.getMessage());
        }
    }
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            if (mCurrentLocation == null) {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            }
        }catch(SecurityException e){
            Log.e(TAG,"onConnected:" + e.getMessage());
        }
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Log.i(TAG,"UPDATE:" + mLastUpdateTime + " ,LON:" + mCurrentLocation.getLongitude()
                + " ,LAT:" + mCurrentLocation.getLatitude());
    }
    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    public void startUpdating() {
        if(mGoogleApiClient.isConnected()) {
            if (!mRequestingLocationUpdates) {
                mRequestingLocationUpdates = true;
                startLocationUpdates();
            }
        }else{
            mGoogleApiClient.connect();
        }
    }
    public void stopUpdating() {
        if(mGoogleApiClient.isConnected()) {
            if (mRequestingLocationUpdates) {
                mRequestingLocationUpdates = false;
                stopLocationUpdates();
            }
        }
    }
    public void updateSetting(int interval){
        stopUpdating();
        createLocationRequest(interval);
        startUpdating();
    }
    public Location getLocationData(){
        return mCurrentLocation;
    }
    public boolean isRequesting(){
        return mRequestingLocationUpdates;
    }
    public void close() {
        mGoogleApiClient.disconnect();
    }
}