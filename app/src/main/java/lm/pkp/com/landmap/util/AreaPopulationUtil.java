package lm.pkp.com.landmap.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.model.AreaAddress;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.area.model.AreaMeasure;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;

/**
 * Created by USER on 11/2/2017.
 */
public class AreaPopulationUtil {

    public static final AreaPopulationUtil INSTANCE = new AreaPopulationUtil();

    private AreaPopulationUtil() {
    }

    public void populateAreaElement(View view) {
        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        this.populateAreaElement(view, areaElement);
    }

    public void populateAreaElement(View view, AreaElement ae) {
        TextView areaNameView = (TextView) view.findViewById(id.area_name_text);
        String areaName = ae.getName();
        if (areaName.length() > 25) {
            areaNameView.setText(areaName.substring(0, 22).concat("..."));
        } else {
            areaNameView.setText(areaName);
        }

        TextView descText = (TextView) view.findViewById(id.area_desc_text);
        String desc = ae.getDescription();
        desc = "<b>Description: </b>" + desc;
        descText.setText(Html.fromHtml(desc));

        TextView addressTextView = (TextView) view.findViewById(id.area_tags_text);
        AreaAddress address = ae.getAddress();
        String areaAddressText = "";
        if (address != null) {
            areaAddressText = address.getDisplaybleAddress();
        }
        String addressContent = "<b>Address: </b>" + areaAddressText;
        addressTextView.setText(Html.fromHtml(addressContent));

        AreaMeasure measure = ae.getMeasure();
        DecimalFormat df = new DecimalFormat("###.##");

        TextView measureText = (TextView) view.findViewById(id.area_measure_text);
        String content = "<b>Area: </b>" + df.format(measure.getSqFeet()) + " Sqft, "
                + df.format(measure.getAcre()) + " Acre, " + df.format(measure.getDecimals()) + " Decimals.";
        measureText.setText(Html.fromHtml(content));

        DriveDBHelper ddh = new DriveDBHelper(view.getContext());
        ImageView areaImgView = (ImageView) view.findViewById(id.area_default_img);
        String thumbRootPath = AreaContext.INSTANCE
                .getAreaLocalPictureThumbnailRoot(ae.getUniqueId()).getAbsolutePath();
        List<DriveResource> imageResources = ddh.fetchImageResources(ae);
        if (imageResources.size() > 0) {
            Bitmap displayBMap = AreaContext.INSTANCE.getDisplayBMap();
            if (displayBMap != null) {
                areaImgView.setImageBitmap(displayBMap);
            } else {
                DriveResource imageResource = imageResources.get(0);
                String imageName = imageResource.getName();
                String thumbnailPath = thumbRootPath + File.separatorChar + imageName;
                File thumbFile = new File(thumbnailPath);
                if (thumbFile.exists()) {
                    displayBMap = BitmapFactory.decodeFile(thumbnailPath);
                    AreaContext.INSTANCE.getViewBitmaps().add(displayBMap);
                    areaImgView.setImageBitmap(displayBMap);
                }
            }
        } else {
            areaImgView.setImageResource(R.drawable.ic_launcher);
        }
    }

}
