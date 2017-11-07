package lm.pkp.com.landmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.res.disp.AreaAddResourceAdaptor;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.util.AreaActivityUtil;

public class AreaAddResourcesActivity extends AppCompatActivity{

    private AreaAddResourceAdaptor adaptor = null;
    private ArrayList<DriveResource> areaResourcesDisplayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_resource_main);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.show();

        AreaActivityUtil.populateAreaElement(this);

        ListView resourceFileList = (ListView) findViewById(R.id.file_display_list);
        adaptor = new AreaAddResourceAdaptor(getApplicationContext(), R.id.file_display_list, areaResourcesDisplayList);
        resourceFileList.setAdapter(adaptor);

        ArrayList<DriveResource> driveResources = AreaContext.getInstance().getUploadedDriveResources();
        areaResourcesDisplayList.clear();
        areaResourcesDisplayList.addAll(driveResources);
        adaptor.notifyDataSetChanged();

        Button takeSnapButton = (Button)findViewById(R.id.take_snap_button);
        takeSnapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AreaAddResourcesActivity.this, AreaCameraPictureActivity.class);
                startActivity(i);
            }
        });

        Button captureVideoButton = (Button)findViewById(R.id.shoot_video_button);
        captureVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AreaAddResourcesActivity.this, AreaCameraVideoActivity.class);
                startActivity(i);
            }
        });

        Button chooseDocumentButton = (Button)findViewById(R.id.add_document_button);
        chooseDocumentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AreaAddResourcesActivity.this, AreaDocumentChooserActivity.class);
                startActivity(i);
            }
        });

        Button driveUploadButton = (Button)findViewById(R.id.upload_to_drive_button);
        driveUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AreaAddResourcesActivity.this, UploadResourcesActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent i = new Intent(AreaAddResourcesActivity.this, PositionMarkerActivity.class);
        startActivity(i);
    }
}
