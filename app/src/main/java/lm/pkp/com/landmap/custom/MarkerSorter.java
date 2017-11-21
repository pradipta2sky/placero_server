package lm.pkp.com.landmap.custom;

import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by USER on 11/19/2017.
 */
public class MarkerSorter {

    public static List<Marker> sortMarkers(List<Marker> markers, final Marker center) {

        Comparator comp = new Comparator<Marker>() {
            @Override
            public int compare(Marker m1, Marker m2) {
                Double distance1 = SphericalUtil.computeDistanceBetween(m1.getPosition()
                        , center.getPosition());
                Double distance2 = SphericalUtil.computeDistanceBetween(m2.getPosition(),
                        center.getPosition());
                return distance1.compareTo(distance2);
            }
        };

        Collections.sort(markers, comp);
        return markers;
    }
}
