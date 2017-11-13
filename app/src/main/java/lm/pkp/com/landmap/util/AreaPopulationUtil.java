package lm.pkp.com.landmap.util;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.sync.LocalFolderStructureManager;

/**
 * Created by USER on 11/2/2017.
 */
public class AreaPopulationUtil {

    public static final AreaPopulationUtil INSTANCE = new AreaPopulationUtil();

    private AreaPopulationUtil(){

    }

    public void populateAreaElement(View view){
        final AreaElement areaElement = AreaContext.getInstance().getAreaElement();
        populateAreaElement(view, areaElement);
    }

    public void populateAreaElement(View view, AreaElement ae){

        TextView areaNameView = (TextView) view.findViewById(R.id.area_name_text);
        String areaName = ae.getName();
        if(areaName.length() > 25){
            areaNameView.setText(areaName.substring(0,22).concat("..."));
        }else {
            areaNameView.setText(areaName);
        }

        TextView descText = (TextView) view.findViewById(R.id.area_desc_text);
        String desc = ae.getDescription();
        desc = "<b>Description: </b>" + desc;
        descText.setText(Html.fromHtml(desc));

        TextView creatorText = (TextView) view.findViewById(R.id.area_creator_text);
        creatorText.setText(Html.fromHtml("<b>Creator: </b>" + ae.getCreatedBy()));

        TextView tagsText = (TextView) view.findViewById(R.id.area_tags_text);
        String areaTags = ae.getAddress();
        String tagsContent = "<b>Address: </b>" + areaTags;
        tagsText.setText(Html.fromHtml(tagsContent));

        double areaMeasureSqFt = ae.getMeasureSqFt();
        double areaMeasureAcre = areaMeasureSqFt / 43560;
        double areaMeasureDecimals = areaMeasureSqFt / 436;
        DecimalFormat df = new DecimalFormat("###.##");

        TextView measureText = (TextView) view.findViewById(R.id.area_measure_text);
        String content = "<b>Area: </b>" + df.format(areaMeasureSqFt) + " Sqft, " + df.format(areaMeasureAcre) +" Acre," +
                df.format(areaMeasureDecimals) + " Decimals.";
        measureText.setText(Html.fromHtml(content));

        final Drawable drawable = view.getBackground().getCurrent();
        if(drawable instanceof GradientDrawable){
            ((GradientDrawable)drawable).setColor(ColorProvider.getAreaDetailsColor(ae));
        }else if(drawable instanceof ColorDrawable){
            ((ColorDrawable)drawable).setColor(ColorProvider.getAreaDetailsColor(ae));
        }

        // Load the area image here.
        final List<DriveResource> driveResources = ae.getDriveResources();
        final Iterator<DriveResource> resIter = driveResources.iterator();
        final LoadStatus loadSuccess = new LoadStatus(false);
        while(resIter.hasNext() && !loadSuccess.isSuccess()){
            final DriveResource res = resIter.next();
            final String resName = res.getName();
            final String resLocalPath = "file://" + LocalFolderStructureManager.getImageStorageDir().getPath()
                    + File.separatorChar + resName;
            if(FileUtil.isImageFile(resLocalPath)){
                ImageView areaImg = (ImageView)view.findViewById(R.id.area_default_img);
                final Picasso picassoElem = Picasso.with(view.getContext());//
                picassoElem.setIndicatorsEnabled(true);
                picassoElem.load(resLocalPath) //
                        .tag(view.getContext()) //
                        .error(R.drawable.app_icon1)
                        .resize(128, 128)
                        .into(areaImg, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                loadSuccess.setSuccess(true);
                            }

                            @Override
                            public void onError() {
                                loadSuccess.setSuccess(false);
                            }
                        });
            }
        }
    }

    private class LoadStatus {

        private boolean success;

        public LoadStatus(boolean status){
            success = status;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }
    }
}
