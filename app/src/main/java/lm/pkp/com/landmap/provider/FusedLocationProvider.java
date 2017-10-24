package lm.pkp.com.landmap.provider;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import lm.pkp.com.landmap.custom.LocationPositionReceiver;
import lm.pkp.com.landmap.position.PositionElement;

/**
 * Created by USER on 10/17/2017.
 */
public class FusedLocationProvider {

    private Activity mContext;

    public FusedLocationProvider(Activity context) {
        this.mContext = context;
    }

    public void getLocation(){

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);

        final LocationManager manager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        final PositionElement pe = new PositionElement();

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                pe.setLon(location.getLongitude());
                pe.setLat(location.getLatitude());
                ((LocationPositionReceiver)mContext).receivedLocationPostion(pe);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        try {
            manager.requestSingleUpdate(criteria, listener, null);
        }catch (SecurityException se){
            se.printStackTrace();
        }
    }
}
