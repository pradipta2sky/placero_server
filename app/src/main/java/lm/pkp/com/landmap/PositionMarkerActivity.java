package lm.pkp.com.landmap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.custom.LocationPositionReceiver;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.position.PostionListAdaptor;
import lm.pkp.com.landmap.provider.GPSLocationProvider;

public class PositionMarkerActivity extends AppCompatActivity implements LocationPositionReceiver {

    private PositionsDBHelper pdb = null;
    private AreaElement ae = null;

    private ArrayList<PositionElement> positionList = new ArrayList<PositionElement>();
    private PostionListAdaptor adaptor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

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

        if(!isGPSEnabled()){
            showLocationDialog();
        }

        pdb = new PositionsDBHelper(getApplicationContext());
        ListView posList = (ListView) findViewById(R.id.positionList);
        adaptor = new PostionListAdaptor(getApplicationContext(), R.id.positionList, positionList);
        posList.setAdapter(adaptor);

        ae = AreaContext.getInstance().getAreaElement();
        positionList.addAll(AreaContext.getInstance().getPositions());
        adaptor.notifyDataSetChanged();

        TextView areaNameView = (TextView)findViewById(R.id.area_name_text);
        areaNameView.setText(ae.getName());

        ActionMenuItemView plotItem = (ActionMenuItemView)findViewById(R.id.action_plot_area);
        plotItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PositionMarkerActivity.this, AreaMapPlotterActivity.class);
                startActivity(intent);
            }
        });

        ActionMenuItemView areaNameEditItem = (ActionMenuItemView)findViewById(R.id.action_area_edit);
        areaNameEditItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent areaEditIntent = new Intent(PositionMarkerActivity.this, AreaEditActivity.class);
                startActivity(areaEditIntent);
            }
        });

        ActionMenuItemView markLocationItem = (ActionMenuItemView)findViewById(R.id.action_mark_location);
        markLocationItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
                new GPSLocationProvider(PositionMarkerActivity.this).getLocation();
            }
        });

        ActionMenuItemView deleteAreaItem = (ActionMenuItemView)findViewById(R.id.action_delete_area);
        deleteAreaItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
                new AreaDBHelper(getApplicationContext()).deleteArea(ae);
                Intent areaDashboardIntent = new Intent(PositionMarkerActivity.this, AreaDashboardActivity.class);
                startActivity(areaDashboardIntent);
            }
        });

        ActionMenuItemView shareAreaItem = (ActionMenuItemView)findViewById(R.id.action_share_area);
        shareAreaItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent areaShareIntent = new Intent(PositionMarkerActivity.this, AreaShareActivity.class);
                startActivity(areaShareIntent);
            }
        });

        ActionMenuItemView navigateItem = (ActionMenuItemView)findViewById(R.id.action_navigate_area);
        navigateItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + ae.getCenterLat()+ "," + ae.getCenterLon());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        ActionMenuItemView driveItem = (ActionMenuItemView)findViewById(R.id.action_drive_area);
        driveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PositionMarkerActivity.this, CreateFolderStructureActivity.class);
                startActivity(intent);
            }
        });

        ActionMenuItemView displayResItem = (ActionMenuItemView)findViewById(R.id.action_display_res);
        displayResItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PositionMarkerActivity.this, AreaResourcesDisplayActivity.class);
                startActivity(intent);
            }
        });

    }

    private void showLocationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("GPS Location disabled.")
                .setMessage("Do you want to enable GPS ?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Add some marker in the context saying that GPS is not enabled.
                        Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }})
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Add some marker in the context saying that GPS is not enabled.
                    }})
                .show();
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
        pe.setName("P_" + UUID.randomUUID().toString());
        pe.setUniqueAreaId(ae.getUniqueId());

        pe = pdb.insertPositionLocally(pe);
        AreaContext.getInstance().addPosition(pe);
        pdb.insertPositionToServer(pe);

        positionList.add(pe);
        adaptor.notifyDataSetChanged();
        findViewById(R.id.splash_panel).setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        Intent areaDashboardIntent = new Intent(PositionMarkerActivity.this, AreaDashboardActivity.class);
        startActivity(areaDashboardIntent);
    }

    private boolean isGPSEnabled(){
        LocationManager locationManager = null;
        boolean gps_enabled= false;

        if(locationManager ==null)
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try{
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){
            //do nothing...
        }
        return gps_enabled;
    }
}
