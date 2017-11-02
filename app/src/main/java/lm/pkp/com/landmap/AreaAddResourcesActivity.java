package lm.pkp.com.landmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaDBHelper;
import lm.pkp.com.landmap.area.AreaDriveResourceAdaptor;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.util.AreaActivityUtil;

public class AreaAddResourcesActivity extends AppCompatActivity{

    private AreaDBHelper adb = null;
    private AreaElement ae = null;

    private AreaDriveResourceAdaptor adaptor = null;
    private ArrayList<DriveResource> areaResources = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_resource_main);

        Bundle bundle = getIntent().getExtras();
        final String areaUid = bundle.getString("area_uid");

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.show();

        adb = new AreaDBHelper(getApplicationContext());
        ae = adb.getAreaByUid(areaUid);
        AreaContext.getInstance().setAreaElement(ae);
        AreaActivityUtil.populateAreaElement(ae, this);

        ListView resourceFileList = (ListView) findViewById(R.id.file_display_list);
        adaptor = new AreaDriveResourceAdaptor(getApplicationContext(), R.id.file_display_list, areaResources);
        resourceFileList.setAdapter(adaptor);

        ArrayList<DriveResource> driveResources = AreaContext.getInstance().getDriveResources();
        areaResources.clear();
        areaResources.addAll(driveResources);
        adaptor.notifyDataSetChanged();

        Button takeSnapButton = (Button)findViewById(R.id.take_snap_button);
        takeSnapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AreaAddResourcesActivity.this, AreaCameraSnapActivity.class);
                startActivity(i);
            }
        });

        Button captureVideoButton = (Button)findViewById(R.id.shoot_video_button);
        captureVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AreaAddResourcesActivity.this, AreaCameraShootActivity.class);
                startActivity(i);
            }
        });

        Button chooseDocumentButton = (Button)findViewById(R.id.upload_document_button);
        chooseDocumentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AreaAddResourcesActivity.this, FCMainActivity.class);
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
        i.putExtra("area_uid", ae.getUniqueId());
        startActivity(i);
    }
}
