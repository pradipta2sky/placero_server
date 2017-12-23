package lm.pkp.com.landmap.position;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serializable;

import lm.pkp.com.landmap.weather.model.WeatherElement;

/**
 * Created by USER on 10/16/2017.
 */
public class PositionElement implements Serializable {

    private String name = "";
    private String description = "No Description";
    private double lat;
    private double lon;
    private String tags = "";
    private String uniqueAreaId = "";
    private String uniqueId = "";
    private String createdOnMillis = System.currentTimeMillis() + "";
    private String type = "boundary";
    private WeatherElement weather;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLat() {
        return this.lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return this.lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getTags() {
        return this.tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getUniqueAreaId() {
        return this.uniqueAreaId;
    }

    public void setUniqueAreaId(String uniqueAreaId) {
        this.uniqueAreaId = uniqueAreaId;
    }

    public String getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public PositionElement copy() {
        return SerializationUtils.clone(this);
    }

    public String getCreatedOnMillis() {
        return createdOnMillis;
    }

    public void setCreatedOnMillis(String createdOnMillis) {
        this.createdOnMillis = createdOnMillis;
    }

    public WeatherElement getWeather() {
        return weather;
    }

    public void setWeather(WeatherElement weather) {
        this.weather = weather;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(lat, ((PositionElement) o).lat);
        builder.append(lon, ((PositionElement) o).lon);
        return builder.isEquals();
    }
}
