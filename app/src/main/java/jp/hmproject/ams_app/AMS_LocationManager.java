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

/**
 * Created by hm on 12/25/2016.
 */
public class AMS_LocationManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener {

    final String TAG = "AMS_LocationManager";
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderApi fusedLocationProviderApi;
    private LocationRequest locationRequest;
    private Location location;

    public AMS_LocationManager(Context ctx) {
        context = ctx;
        fusedLocationProviderApi = LocationServices.FusedLocationApi;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG,"onConnectionFailed:" + connectionResult.getErrorMessage());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startService();
    }
    @Override
    public void onConnectionSuspended(int i) {

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
        mGoogleApiClient.connect();
    }
    public void startService(){
        try{
            Location currentLocation = fusedLocationProviderApi.getLastLocation(mGoogleApiClient);
            if(currentLocation != null){
                location = currentLocation;
            }else{
                try{
                    fusedLocationProviderApi.requestLocationUpdates(
                            mGoogleApiClient,locationRequest, (LocationListener) this);

                }catch (Exception e){
                    Log.e(TAG,"onConnected:" + e.getMessage());
                }
            }
        }catch(SecurityException e){
            Log.e(TAG,"onConnected:" + e.getMessage());
        }
    }
    public void stopService(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }
    public Location getLocationData(){
        return location;
    }
}
