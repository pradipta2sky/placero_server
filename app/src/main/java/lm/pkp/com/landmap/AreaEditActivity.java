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

public class AreaEditActivity extends AppCompatActivity{

    private AreaDBHelper adb = null;
    private AreaElement ae = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_edit);

        Bundle bundle = getIntent().getExtras();
        final String areaName = bundle.getString("area_name");

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.show();

        adb = new AreaDBHelper(getApplicationContext());
        final AreaElement ae = adb.getAreaByName(areaName);

        final TextView areaNameView = (TextView)findViewById(R.id.area_name_text);
        areaNameView.setText(ae.getName());

        final TextView areaDescView = (TextView)findViewById(R.id.area_desc_text);
        areaDescView.setText(ae.getDescription());

        final TextView areaCreatorView = (TextView)findViewById(R.id.area_creator_text);
        areaCreatorView.setText(ae.getCreatedBy());

        final TextView areaTagsView = (TextView)findViewById(R.id.area_tags_text);
        areaTagsView.setText(ae.getTags());

        final EditText nameTextView = (EditText) findViewById(R.id.area_name_edit);
        nameTextView.setText(ae.getName());

        final EditText descTextView = (EditText) findViewById(R.id.area_desc_edit);
        descTextView.setText(ae.getDescription());

        double areaMeasureSqFt = ae.getMeasureSqFt();
        double areaMeasureAcre = areaMeasureSqFt / 43560;
        double areaMeasureDecimals = areaMeasureSqFt / 436;
        DecimalFormat df = new DecimalFormat("###.##");

        TextView measureText = (TextView) findViewById(R.id.area_measure_text);
        String content = "Area: " + df.format(areaMeasureSqFt) + " Sqft, " + df.format(areaMeasureAcre) +" Acre," +
                df.format(areaMeasureDecimals) + " Decimals.";
        measureText.setText(content);

        Button saveButton = (Button)findViewById(R.id.area_edit_save_btn);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

                String nameText = nameTextView.getText().toString();
                ae.setName(nameText);

                String descText = descTextView.getText().toString();
                ae.setDescription(descText);

                adb.updateAreaNonGeo(ae);
                findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);

                Intent positionMarkerIntent = new Intent(AreaEditActivity.this, PositionMarkerActivity.class);
                positionMarkerIntent.putExtra("area_name", ae.getName());
                startActivity(positionMarkerIntent);
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
