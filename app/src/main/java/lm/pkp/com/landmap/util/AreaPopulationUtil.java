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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.model.AreaElement;
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

        TextView tagsText = (TextView) view.findViewById(id.area_tags_text);
        String areaAddress = ae.getAddress();
        areaAddress = areaAddress.replaceAll(Pattern.quote("@$"), "");
        String tagsContent = "<b>Address: </b>" + areaAddress;
        tagsText.setText(Html.fromHtml(tagsContent));

        double areaMeasureSqFt = ae.getMeasureSqFt();
        double areaMeasureAcre = areaMeasureSqFt / 43560;
        double areaMeasureDecimals = areaMeasureSqFt / 436;
        DecimalFormat df = new DecimalFormat("###.##");

        TextView measureText = (TextView) view.findViewById(id.area_measure_text);
        String content = "<b>Area: </b>" + df.format(areaMeasureSqFt) + " Sqft, "
                + df.format(areaMeasureAcre) + " Acre, " + df.format(areaMeasureDecimals) + " Decimals.";
        measureText.setText(Html.fromHtml(content));

        Drawable drawable = view.getBackground().getCurrent();
        if (drawable instanceof GradientDrawable) {
            ((GradientDrawable) drawable).setColor(ColorProvider.getAreaDetailsColor(ae));
        } else if (drawable instanceof ColorDrawable) {
            ((ColorDrawable) drawable).setColor(ColorProvider.getAreaDetailsColor(ae));
        }

        DriveDBHelper ddh = new DriveDBHelper(view.getContext());
        ImageView areaImg = (ImageView) view.findViewById(id.area_default_img);

        String thumbRootPath = AreaContext.INSTANCE
                .getAreaLocalPictureThumbnailRoot(ae.getUniqueId()).getAbsolutePath();

        Bitmap displayBMap = AreaContext.INSTANCE.getDisplayBMap();
        if(displayBMap != null){
            areaImg.setImageBitmap(displayBMap);
        }else {
            List<DriveResource> imageResources = ddh.fetchImageResources(ae);
            Iterator<DriveResource> imageResIter = imageResources.iterator();
            while (imageResIter.hasNext()) {
                DriveResource imageResource = imageResIter.next();
                String imageName = imageResource.getName();
                String thumbnailPath = thumbRootPath + File.separatorChar + imageName;
                File thumbFile = new File(thumbnailPath);
                if (thumbFile.exists()) {
                    displayBMap = BitmapFactory.decodeFile(thumbnailPath);
                    AreaContext.INSTANCE.getViewBitmaps().add(displayBMap);
                    areaImg.setImageBitmap(displayBMap);
                }
                break;
            }
        }
    }

}
