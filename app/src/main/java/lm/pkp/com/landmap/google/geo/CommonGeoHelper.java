package lm.pkp.com.landmap.google.geo;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lm.pkp.com.landmap.area.model.AreaAddress;

/**
 * Created by USER on 11/8/2017.
 */
public class CommonGeoHelper {

    public static final CommonGeoHelper INSTANCE = new CommonGeoHelper();

    private CommonGeoHelper() {
    }

    public Map<String,String> getAddressByGeoLocation(Context context, Double lat, Double lon) {
        Map<String, String> addressStore = new HashMap<>();
        try {
            Location areaLocation = new Location("");
            areaLocation.setLatitude(lat);
            areaLocation.setLongitude(lon);

            Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);
            List<Address> addresses = geocoder.getFromLocation(areaLocation.getLatitude(), areaLocation.getLongitude(), 1);
            for (int i = 0; i < addresses.size(); i++) {
                Address address = addresses.get(i);
                int maxLine = address.getMaxAddressLineIndex();
                for (int j = 0; j <= maxLine; j++) {
                    addressStore.put("line_" + j, address.getAddressLine(j));
                }
            }
        } catch (Exception e) {
            // Do nothing if fails.
        }
        return addressStore;
    }
}
