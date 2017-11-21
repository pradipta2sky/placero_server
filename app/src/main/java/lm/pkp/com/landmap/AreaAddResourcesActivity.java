package lm.pkp.com.landmap;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.R.id;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.res.disp.AreaAddResourceAdaptor;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.util.AreaPopulationUtil;
import lm.pkp.com.landmap.util.ColorProvider;

public class AreaAddResourcesActivity extends AppCompatActivity {

    private AreaAddResourceAdaptor adaptor;
    private final ArrayList<DriveResource> areaResourcesDisplayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        this.setContentView(layout.activity_area_resource_main);

        ActionBar ab = this.getSupportActionBar();
        ab.setHomeButtonEnabled(false);
        ab.setDisplayHomeAsUpEnabled(false);

        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        ab.setBackgroundDrawable(new ColorDrawable(ColorProvider.getAreaToolBarColor(areaElement)));
        ab.show();

        View includedView = this.findViewById(R.id.selected_area_include);
        AreaPopulationUtil.INSTANCE.populateAreaElement(includedView);

        ListView resourceFileList = (ListView) this.findViewById(R.id.file_display_list);
        this.adaptor = new AreaAddResourceAdaptor(this.getApplicationContext(), R.id.file_display_list, this.areaResourcesDisplayList);
        resourceFileList.setAdapter(this.adaptor);

        ArrayList<DriveResource> driveResources = AreaContext.INSTANCE.getUploadedQueue();
        this.areaResourcesDisplayList.clear();
        this.areaResourcesDisplayList.addAll(driveResources);
        this.adaptor.notifyDataSetChanged();

        Button takeSnapButton = (Button) this.findViewById(R.id.take_snap_button);
        takeSnapButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AreaAddResourcesActivity.this, AreaCameraPictureActivity.class);
                AreaAddResourcesActivity.this.startActivity(i);
            }
        });

        Button captureVideoButton = (Button) this.findViewById(R.id.shoot_video_button);
        captureVideoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AreaAddResourcesActivity.this, AreaCameraVideoActivity.class);
                AreaAddResourcesActivity.this.startActivity(i);
            }
        });

        Button chooseDocumentButton = (Button) this.findViewById(R.id.add_document_button);
        chooseDocumentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AreaAddResourcesActivity.this, AreaDocumentChooserActivity.class);
                AreaAddResourcesActivity.this.startActivity(i);
            }
        });

        Button driveUploadButton = (Button) this.findViewById(R.id.upload_to_drive_button);
        driveUploadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AreaAddResourcesActivity.this.adaptor.getCount() == 0){
                    AreaAddResourcesActivity.this.showErrorMessage("Nothing to upload.", "error");
                    return;
                }
                Intent i = new Intent(AreaAddResourcesActivity.this, UploadResourcesActivity.class);
                AreaAddResourcesActivity.this.startActivity(i);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        this.finish();
        Intent i = new Intent(this, AreaDetailsActivity.class);
        this.startActivity(i);
    }

    private void showErrorMessage(String message, String type) {
        final Snackbar snackbar = Snackbar.make(this.getWindow().getDecorView(), message, Snackbar.LENGTH_INDEFINITE);

        View sbView = snackbar.getView();
        snackbar.getView().setBackgroundColor(Color.WHITE);

        TextView textView = (TextView) sbView.findViewById(id.snackbar_text);
        if(type.equalsIgnoreCase("info")){
            textView.setTextColor(Color.GREEN);
        } else if(type.equalsIgnoreCase("error")) {
            textView.setTextColor(Color.RED);
        }else{
            textView.setTextColor(Color.DKGRAY);
        }
        textView.setTypeface(Typeface.SANS_SERIF,Typeface.BOLD);
        textView.setTextSize(15);
        textView.setMaxLines(3);

        snackbar.setAction("Dismiss", new OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

}
