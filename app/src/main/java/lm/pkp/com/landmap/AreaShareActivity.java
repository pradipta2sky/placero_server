package lm.pkp.com.landmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

import lm.pkp.com.landmap.area.AreaDBHelper;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.user.UserInfoSearchAsyncTask;

public class AreaShareActivity extends AppCompatActivity{

    private AreaDBHelper adh = null;
    private PositionsDBHelper pdh = null;
    private AreaElement ae = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_share);

        Bundle bundle = getIntent().getExtras();
        final String areaName = bundle.getString("area_name");

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.show();

        adh = new AreaDBHelper(getApplicationContext());
        pdh = new PositionsDBHelper(getApplicationContext());
        final AreaElement ae = adh.getAreaByName(areaName);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, new String[]{});
        final AutoCompleteTextView userIdView = (AutoCompleteTextView) findViewById(R.id.user_search_text);
        userIdView.setAdapter(adapter);

        userIdView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                UserInfoSearchAsyncTask searcherTask = new UserInfoSearchAsyncTask(adapter);
                JSONObject searchParams = new JSONObject();
                try {
                    searchParams.put("us", s.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                searcherTask.execute(searchParams);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        TextView areaNameView = (TextView)findViewById(R.id.area_name_text);
        if(areaName.length() > 25){
            areaNameView.setText(areaName.substring(0,22).concat("..."));
        }else {
            areaNameView.setText(areaName);
        }

        Button saveButton = (Button)findViewById(R.id.area_share_save_btn);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);

                AreaElement copiedArea = ae.copy();
                copiedArea.setCurrentOwner(userIdView.getText().toString());
                copiedArea.setUniqueId(UUID.randomUUID().toString());
                adh.insertAreaToServer(copiedArea);

                List<PositionElement> positions = copiedArea.getPositions();
                for (int i = 0; i < positions.size(); i++) {
                    PositionElement pe = positions.get(i);
                    pe.setUniqueId(UUID.randomUUID().toString());
                    pe.setUniqueAreaId(copiedArea.getUniqueId());
                    pdh.insertPositionToServer(pe);
                }
                findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);

                Intent positionMarkerIntent = new Intent(AreaShareActivity.this, PositionMarkerActivity.class);
                positionMarkerIntent.putExtra("area_name", ae.getName());
                startActivity(positionMarkerIntent);

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent positionMarkerIntent = new Intent(AreaShareActivity.this, PositionMarkerActivity.class);
                positionMarkerIntent.putExtra("area_name", ae.getName());
                startActivity(positionMarkerIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Intent positionMarkerIntent = new Intent(AreaShareActivity.this, PositionMarkerActivity.class);
        positionMarkerIntent.putExtra("area_name", ae.getName());
        startActivity(positionMarkerIntent);
    }

}
