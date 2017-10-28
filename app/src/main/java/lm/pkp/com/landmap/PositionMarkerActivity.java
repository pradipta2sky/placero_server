package lm.pkp.com.landmap;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import lm.pkp.com.landmap.area.AreaDBHelper;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.custom.LocationPositionReceiver;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.position.PostionListAdaptor;
import lm.pkp.com.landmap.provider.FusedLocationProvider;
import lm.pkp.com.landmap.provider.GPSLocationProvider;

public class PositionMarkerActivity extends AppCompatActivity implements LocationPositionReceiver {

    private AreaDBHelper adb = null;
    private PositionsDBHelper pdb = null;
    private AreaElement ae = null;

    private ArrayList<PositionElement> positionList = new ArrayList<PositionElement>();
    private PostionListAdaptor adaptor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_marker);
        getSupportActionBar().hide();

        Toolbar topTB = (Toolbar) findViewById(R.id.toolbar_top);
        topTB.inflateMenu(R.menu.marking_top_menu);
        Toolbar bottomTB = (Toolbar) findViewById(R.id.toolbar_bottom);
        bottomTB.inflateMenu(R.menu.marking_bottom_menu);

        if(!approachLocationPermissions()){
            Toast.makeText(getApplicationContext(),"No permission for location access.", Toast.LENGTH_LONG);
            finish();
        }

        adb = new AreaDBHelper(getApplicationContext());
        pdb = new PositionsDBHelper(getApplicationContext());

        ListView posList = (ListView) findViewById(R.id.positionList);
        adaptor = new PostionListAdaptor(getApplicationContext(), R.id.positionList, positionList);
        posList.setAdapter(adaptor);

        Bundle bundle = getIntent().getExtras();
        String areaName = bundle.getString("area_name");
        ae = adb.getAreaByName(areaName);
        positionList.addAll(ae.getPositions());
        adaptor.notifyDataSetChanged();

        TextView areaNameView = (TextView)findViewById(R.id.area_name_text);
        if(areaName.length() > 20){
            areaNameView.setText(areaName.substring(0,17).concat("..."));
        }else {
            areaNameView.setText(areaName);
        }

        ActionMenuItemView plotItem = (ActionMenuItemView)findViewById(R.id.action_plot_area);
        plotItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PositionMarkerActivity.this, AreaPlotterActivity.class);
                intent.putExtra("area_name", ae.getName());
                startActivity(intent);
            }
        });

        ActionMenuItemView areaNameEditItem = (ActionMenuItemView)findViewById(R.id.action_area_name_edit);
        areaNameEditItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent areaEditIntent = new Intent(PositionMarkerActivity.this, AreaEditActivity.class);
                areaEditIntent.putExtra("area_name", ae.getName());
                startActivity(areaEditIntent);
            }
        });

        ActionMenuItemView markLocationItem = (ActionMenuItemView)findViewById(R.id.action_mark_location);
        markLocationItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                new GPSLocationProvider(PositionMarkerActivity.this).getLocation();
            }
        });

        ActionMenuItemView deleteAreaItem = (ActionMenuItemView)findViewById(R.id.action_delete_area);
        deleteAreaItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                adb.deleteArea(ae);
                Intent areaDashboardIntent = new Intent(PositionMarkerActivity.this, AreaDashboardActivity.class);
                startActivity(areaDashboardIntent);
            }
        });

        ActionMenuItemView shareAreaItem = (ActionMenuItemView)findViewById(R.id.action_share_area);
        shareAreaItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent areaShareIntent = new Intent(PositionMarkerActivity.this, AreaShareActivity.class);
                areaShareIntent.putExtra("area_name", ae.getName());
                startActivity(areaShareIntent);
            }
        });
    }

    private boolean approachLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},1);
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                return true;
            }else {
                return false;
            }
        }
    }

    @Override
    public void receivedLocationPostion(PositionElement pe) {
        pe.setName("Position_" + positionList.size());
        pe = pdb.insertPositionLocally(pe, ae);
        pdb.insertPositionToServer(pe);

        positionList.add(pe);
        adaptor.notifyDataSetChanged();
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        Intent areaDashboardIntent = new Intent(PositionMarkerActivity.this, AreaDashboardActivity.class);
        startActivity(areaDashboardIntent);
    }
}
