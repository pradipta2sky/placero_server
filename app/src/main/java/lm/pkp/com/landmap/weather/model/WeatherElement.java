package lm.pkp.com.landmap.weather.model;

import java.util.ArrayList;
import java.util.List;

import lm.pkp.com.landmap.weather.model.ForecastElement;

/**
 * Created by USER on 11/19/2017.
 */
public class WeatherElement {

    private String uniqueId = "";
    private String positionId = "";
    private String temperature = "";
    private String conditionText = "";
    private String conditionCode = "";
    private String address = "";
    private String windChill = "";
    private String windDirection = "";
    private String windSpeed = "";
    private String humidity = "";
    private String visibility = "";
    private String createdOn = "";
    private List<ForecastElement> forecast = new ArrayList<>();

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getConditionText() {
        return this.conditionText;
    }

    public void setConditionText(String conditionText) {
        this.conditionText = conditionText;
    }

    public String getConditionCode() {
        return this.conditionCode;
    }

    public void setConditionCode(String conditionCode) {
        this.conditionCode = conditionCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWindChill() {
        return windChill;
    }

    public void setWindChill(String windChill) {
        this.windChill = windChill;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public List<ForecastElement> getForecast() {
        return this.forecast;
    }

    public void setForecast(List<ForecastElement> forecast) {
        this.forecast = forecast;
    }

    public String getCreatedOn() {
        return this.createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
