package lm.pkp.com.landmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;

import lm.pkp.com.landmap.area.AreaDBHelper;
import lm.pkp.com.landmap.area.AreaElement;

public class AreaAddResourcesActivity extends AppCompatActivity{

    private AreaDBHelper adb = null;
    private AreaElement ae = null;

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

        final TextView areaNameView = (TextView)findViewById(R.id.area_name_text);
        areaNameView.setText(ae.getName());

        final TextView areaDescView = (TextView)findViewById(R.id.area_desc_text);
        areaDescView.setText(ae.getDescription());

        final TextView areaCreatorView = (TextView)findViewById(R.id.area_creator_text);
        areaCreatorView.setText(ae.getCreatedBy());

        final TextView areaTagsView = (TextView)findViewById(R.id.area_tags_text);
        areaTagsView.setText(ae.getTags());

        double areaMeasureSqFt = ae.getMeasureSqFt();
        double areaMeasureAcre = areaMeasureSqFt / 43560;
        double areaMeasureDecimals = areaMeasureSqFt / 436;
        DecimalFormat df = new DecimalFormat("###.##");

        TextView measureText = (TextView) findViewById(R.id.area_measure_text);
        String content = "Area: " + df.format(areaMeasureSqFt) + " Sqft, " + df.format(areaMeasureAcre) +" Acre," +
                df.format(areaMeasureDecimals) + " Decimals.";
        measureText.setText(content);

        Button takeSnapButton = (Button)findViewById(R.id.take_snap_button);
        takeSnapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AreaAddResourcesActivity.this, AreaCameraShootActivity.class);
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
    }
}
