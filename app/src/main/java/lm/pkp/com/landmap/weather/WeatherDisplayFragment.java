package lm.pkp.com.landmap.weather;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.weather.model.WeatherElement;
import lm.pkp.com.landmap.weather.util.WindInterpreter;

/**
 * Created by USER on 11/20/2017.
 */
public class WeatherDisplayFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.area_weather_widget, container, false);
        PositionElement centerPosition = AreaContext.INSTANCE.getAreaElement().getCenterPosition();
        WeatherElement weather = centerPosition.getWeather();

        TextView addressText = (TextView) rootView.findViewById(R.id.w_address);
        addressText.setText(weather.getAddress());

        TextView updatedText = (TextView) rootView.findViewById(R.id.w_updated);
        Long createdMillis = new Long(weather.getCreatedOn());
        CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(createdMillis,
                System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
        updatedText.setText(timeSpan.toString());

        TextView weatherIcon = (TextView) rootView.findViewById(R.id.weather_icon);
        weatherIcon.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/weathericons-regular-webfont.ttf"));
        weatherIcon.setText(getWeatherIcon(new Integer(weather.getConditionCode())));

        TextView condition = (TextView) rootView.findViewById(R.id.w_condition);
        condition.setText(weather.getConditionText());

        TextView temperature = (TextView) rootView.findViewById(R.id.w_temperature);
        temperature.setText("Temperature " + convertFarenheitToCelcius(weather.getTemperature()) + " \u2103");

        TextView humidity = (TextView) rootView.findViewById(R.id.w_humidity);
        humidity.setText("Humidity " + weather.getHumidity() + " %");

        TextView windDetails = (TextView) rootView.findViewById(R.id.w_wind_details);
        String windChill = convertFarenheitToCelcius(weather.getWindChill());
        String windDirection
                = WindInterpreter.getDirectionFromBearing(Double.parseDouble(weather.getWindDirection()));
        String windSpeed = convertMilesToKms(weather.getWindSpeed());
        String windText = "Wind @" + windSpeed + " km/hr, "
                + windDirection + ", " + windChill + " \u2103";
        windDetails.setText(windText);

        // Fill the data here
        return rootView;
    }

    public String convertFarenheitToCelcius(String celcius) {
        int celInt = Integer.parseInt(celcius);
        double farenheit = (celInt - 32) * 5 / 9;
        return farenheit + "";
    }

    public String convertMilesToKms(String miles) {
        float milesFloat = Float.parseFloat(miles);
        double kilometers = milesFloat * 1.609344;
        return String.format("%.2f", kilometers);
    }

    private String getWeatherIcon(int code) {
        String prefix = "wi_yahoo_";
        String iconText = prefix + code;
        int resId = getResources().getIdentifier(iconText, "string", getActivity().getPackageName());
        return getActivity().getString(resId);
    }
}
