package lm.pkp.com.landmap.position;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

/**
 * Created by USER on 10/16/2017.
 */
public class PositionElement implements Serializable {

    private String name = "";
    private String description = "";
    private double lat = 0;
    private double lon = 0;
    private String tags = "";
    private String uniqueAreaId = "";
    private String uniqueId = "";
    private String createdOnMillis = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getUniqueAreaId() {
        return uniqueAreaId;
    }

    public void setUniqueAreaId(String uniqueAreaId) {
        this.uniqueAreaId = uniqueAreaId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public PositionElement copy() {
        return SerializationUtils.clone(this);
    }

    public String getCreatedOnMillis() {
        return this.createdOnMillis;
    }

    public void setCreatedOnMillis(String createdOnMillis) {
        this.createdOnMillis = createdOnMillis;
    }

}
