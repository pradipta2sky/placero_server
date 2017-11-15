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

import lm.pkp.com.landmap.custom.LocationPositionReceiver;
import lm.pkp.com.landmap.position.PositionElement;

public class GPSLocationProvider implements LocationListener {

    private final Activity activity;

    public GPSLocationProvider(Activity context) {
        this.activity = context;
    }

    public void getLocation() {
        try {
            final LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            Looper looper = Looper.myLooper();
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
            final Handler handler = new Handler(looper);
            handler.postDelayed(new Runnable() {
                public void run() {
                    locationManager.removeUpdates(GPSLocationProvider.this);
                    notifyFailureForLocationFix();
                }
            }, (1000 * 30)); // timeout after 30 secs
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        PositionElement pe = new PositionElement();
        pe.setLon(location.getLongitude());
        pe.setLat(location.getLatitude());
        ((LocationPositionReceiver) activity).receivedLocationPostion(pe);
    }

    @Override
    public void onProviderDisabled(String provider) {
        ((LocationPositionReceiver) activity).providerDisabled();
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void notifyFailureForLocationFix() {
        ((LocationPositionReceiver) activity).locationFixTimedOut();
    }

}