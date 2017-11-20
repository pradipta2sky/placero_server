package lm.pkp.com.landmap.weather.model;

/**
 * Created by USER on 11/19/2017.
 */
public class ForecastElement {

    private String message = "";
    private String date = "";
    private String day = "";
    private String temperatureHigh = "";
    private String temperatureLow = "";

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDay() {
        return this.day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTemperatureHigh() {
        return this.temperatureHigh;
    }

    public void setTemperatureHigh(String temperatureHigh) {
        this.temperatureHigh = temperatureHigh;
    }

    public String getTemperatureLow() {
        return this.temperatureLow;
    }

    public void setTemperatureLow(String temperatureLow) {
        this.temperatureLow = temperatureLow;
    }
}
