package lm.pkp.com.landmap.util;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import java.text.DecimalFormat;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;

/**
 * Created by USER on 11/2/2017.
 */
public class AreaActivityUtil {

    public static void populateAreaElement(Activity activity){
        AreaElement ae = AreaContext.getInstance().getAreaElement();

        TextView areaNameView = (TextView)activity.findViewById(R.id.area_name_text);
        areaNameView.setText(ae.getName());

        TextView areaDescView = (TextView)activity.findViewById(R.id.area_desc_text);
        areaDescView.setText(ae.getDescription());

        TextView areaCreatorView = (TextView)activity.findViewById(R.id.area_creator_text);
        areaCreatorView.setText(ae.getCreatedBy());

        TextView areaTagsView = (TextView)activity.findViewById(R.id.area_tags_text);
        areaTagsView.setText(ae.getTags());

        double areaMeasureSqFt = ae.getMeasureSqFt();
        double areaMeasureAcre = areaMeasureSqFt / 43560;
        double areaMeasureDecimals = areaMeasureSqFt / 436;
        DecimalFormat df = new DecimalFormat("###.##");

        TextView measureText = (TextView) activity.findViewById(R.id.area_measure_text);
        String content = "Area: " + df.format(areaMeasureSqFt) + " Sqft, " + df.format(areaMeasureAcre) +" Acre," +
                df.format(areaMeasureDecimals) + " Decimals.";
        measureText.setText(content);
    }
}
