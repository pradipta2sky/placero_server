package lm.pkp.com.landmap.provider;

/**
 * Created by USER on 10/16/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.UUID;

import lm.pkp.com.landmap.custom.LocationPositionReceiver;
import lm.pkp.com.landmap.position.PositionElement;

public class GPSLocationProvider implements LocationListener {

    private final Activity activity;
    private LocationPositionReceiver receiver = null;
    private int timeout = 60;
    private PositionElement pe = new PositionElement();

    public GPSLocationProvider(Activity activity) {
        this.activity = activity;
    }

    public GPSLocationProvider(Activity activity, LocationPositionReceiver receiver) {
        this.activity = activity;
        this.receiver = receiver;
    }

    public GPSLocationProvider(Activity activity, LocationPositionReceiver receiver, int timeoutSecs) {
        this.activity = activity;
        this.receiver = receiver;
        this.timeout = timeoutSecs;
    }

    public void getLocation() {
        try {
            final LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);

            Looper looper = Looper.myLooper();
            final Handler handler = new Handler(looper);
            handler.postDelayed(new Runnable() {
                public void run() {
                    locationManager.removeUpdates(GPSLocationProvider.this);
                    if(pe.getUniqueId().equalsIgnoreCase("")){
                        notifyFailureForLocationFix();
                    }
                }
            }, (1000 * timeout));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        pe.setUniqueId(UUID.randomUUID().toString());
        pe.setLon(location.getLongitude());
        pe.setLat(location.getLatitude());
        if(receiver != null){
            receiver.receivedLocationPostion(pe);
        }else {
            ((LocationPositionReceiver) activity).receivedLocationPostion(pe);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if(receiver != null){
            receiver.providerDisabled();
        }else {
            ((LocationPositionReceiver) activity).providerDisabled();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void notifyFailureForLocationFix() {
        if(receiver != null){
            receiver.locationFixTimedOut();
        }else {
            ((LocationPositionReceiver) activity).locationFixTimedOut();
        }
    }

}