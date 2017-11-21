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
    private LocationPositionReceiver receiver;
    private int timeout = 30;
    private final PositionElement pe = new PositionElement();

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
        timeout = timeoutSecs;
    }

    public void getLocation() {
        try {
            final LocationManager locationManager = (LocationManager) this.activity.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);

            Looper looper = Looper.myLooper();
            Handler handler = new Handler(looper);
            handler.postDelayed(new Runnable() {
                public void run() {
                    locationManager.removeUpdates(GPSLocationProvider.this);
                    String uniqueId = GPSLocationProvider.this.pe.getUniqueId();
                    if (uniqueId.equalsIgnoreCase("")) {
                        GPSLocationProvider.this.notifyFailureForLocationFix();
                    }
                }
            }, 1000 * timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        pe.setUniqueId(UUID.randomUUID().toString());
        pe.setLon(location.getLongitude());
        pe.setLat(location.getLatitude());
        pe.setCreatedOnMillis(System.currentTimeMillis() + "");

        if (this.receiver != null) {
            this.receiver.receivedLocationPostion(this.pe);
        } else {
            ((LocationPositionReceiver) this.activity).receivedLocationPostion(this.pe);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (this.receiver != null) {
            this.receiver.providerDisabled();
        } else {
            ((LocationPositionReceiver) this.activity).providerDisabled();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void notifyFailureForLocationFix() {
        if (this.receiver != null) {
            this.receiver.locationFixTimedOut();
        } else {
            ((LocationPositionReceiver) this.activity).locationFixTimedOut();
        }
    }

}