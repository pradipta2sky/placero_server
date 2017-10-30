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

import lm.pkp.com.landmap.custom.LocationPositionReceiver;
import lm.pkp.com.landmap.position.PositionElement;

public class GPSLocationProvider implements LocationListener {

    private final Activity mContext;
    private PositionElement pe = null;

    public GPSLocationProvider(Activity context) {
        this.mContext = context;
    }

    public void getLocation() {
        try {
            pe = new PositionElement();
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,this,null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        pe.setLon(location.getLongitude());
        pe.setLat(location.getLatitude());
        ((LocationPositionReceiver)mContext).receivedLocationPostion(pe);
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Show an error to the user.
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

}