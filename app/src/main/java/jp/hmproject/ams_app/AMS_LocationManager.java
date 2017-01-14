package jp.hmproject.ams_app;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
    final String TAG = "AMS_LocationManager";
    private AMS_LocationManagerListener listener;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;

    protected Boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;
    final int DefaultInterval = 5;

    public AMS_LocationManager(Context context) {
        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest(DefaultInterval);
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
        Log.d(TAG,"startLocationUpdates");
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }catch (SecurityException e){
            Log.e(TAG,"startLocation:" + e.getMessage());
        }
    }
    protected void stopLocationUpdates() {
        Log.d(TAG,"stopLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        listener.locationServiceConnected();
        Log.d(TAG,"onConnected");
        try {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mCurrentLocation != null){
                mLastUpdateTime = DateFormat.getTimeInstance().format(mCurrentLocation.getTime());
                Log.d(TAG,"onConnected/getLastLocation:Date:" + mLastUpdateTime
                        + " ,LON:" + mCurrentLocation.getLongitude()
                        + " ,LAT:" + mCurrentLocation.getLatitude());
            }
        }catch(SecurityException e){
            Log.e(TAG,"onConnected:" + e.getMessage());
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        listener.changeLocationData();
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(mCurrentLocation.getTime());
        Log.d(TAG,"onLocationChanged:Date:" + mLastUpdateTime
                + " ,LON:" + mCurrentLocation.getLongitude()
                + " ,LAT:" + mCurrentLocation.getLatitude());
        stopLocationUpdates();
    }
    @Override
    public void onConnectionSuspended(int cause) {
        Log.e(TAG, "onConnectionSuspended:" + cause);
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "onConnectionFailed:" + result.getErrorCode());
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
            mRequestingLocationUpdates = false;
            stopLocationUpdates();
        }
    }
    public void updateSetting(int interval){
        stopUpdating();
        createLocationRequest(interval);
    }
    public Location getLocationData(){
        return mCurrentLocation;
    }
    public void close() {
        mGoogleApiClient.disconnect();
    }
    public void setListener(AMS_LocationManagerListener listener){
        this.listener = listener;
    }
}