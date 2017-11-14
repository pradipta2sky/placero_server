package lm.pkp.com.landmap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.custom.LocationPositionReceiver;
import lm.pkp.com.landmap.permission.PermissionConstants;
import lm.pkp.com.landmap.permission.PermissionManager;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.position.PostionListAdaptor;
import lm.pkp.com.landmap.provider.GPSLocationProvider;
import lm.pkp.com.landmap.util.ColorProvider;

public class AreaDetailsActivity extends AppCompatActivity implements LocationPositionReceiver {

    private PositionsDBHelper pdb;
    private AreaElement ae;

    private ArrayList<PositionElement> positionList = new ArrayList<PositionElement>();
    private PostionListAdaptor adaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);
        setContentView(R.layout.activity_area_details);
        getSupportActionBar().hide();

        ae = AreaContext.getInstance().getAreaElement();
        Toolbar topTB = (Toolbar) findViewById(R.id.toolbar_top);
        final ColorDrawable topDrawable = (ColorDrawable) topTB.getBackground().getCurrent();
        topDrawable.setColor(ColorProvider.getAreaToolBarColor(ae));

        Toolbar bottomTB = (Toolbar) findViewById(R.id.toolbar_bottom);
        final ColorDrawable bottomDrawable = (ColorDrawable) bottomTB.getBackground().getCurrent();
        bottomDrawable.setColor(ColorProvider.getAreaToolBarColor(ae));

        if (!askForLocationPermission()) {
            Toast.makeText(getApplicationContext(), "No permission for location access.", Toast.LENGTH_LONG);
            finish();
        }

        if (!isGPSEnabled()) {
            showLocationDialog();
        }

        pdb = new PositionsDBHelper(getApplicationContext());
        ListView posListView = (ListView) findViewById(R.id.positionList);
        positionList.addAll(AreaContext.getInstance().getPositions());
        adaptor = new PostionListAdaptor(getApplicationContext(), R.id.positionList, positionList);
        posListView.setAdapter(adaptor);
        adaptor.notifyDataSetChanged();

        TextView areaNameView = (TextView) findViewById(R.id.area_name_text);
        String areaName = ae.getName();
        if (areaName.length() > 20) {
            areaName = areaName.substring(0, 19).concat("...");
        }
        areaNameView.setText(areaName);

        ImageView areaEditItem = (ImageView) findViewById(R.id.action_area_edit);
        areaEditItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent areaEditIntent = new Intent(AreaDetailsActivity.this, AreaEditActivity.class);
                startActivity(areaEditIntent);
            }
        });

        ImageView deleteAreaItem = (ImageView) findViewById(R.id.action_delete_area);
        deleteAreaItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.REMOVE_AREA)) {
                    findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
                    final AreaDBHelper adh = new AreaDBHelper(getApplicationContext(), new DeleteAreaCallback());
                    adh.deleteArea(ae);
                    adh.deleteAreaFromServer(ae);
                } else {
                    Toast.makeText(getApplicationContext(), "You do not enough rights !!", Toast.LENGTH_LONG).show();
                }
            }
        });

        ImageView markLocationItem = (ImageView) findViewById(R.id.action_mark_location);
        markLocationItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.MARK_POSITION)) {
                    findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
                    new GPSLocationProvider(AreaDetailsActivity.this).getLocation();
                } else {
                    Toast.makeText(getApplicationContext(), "You do not enough rights !!", Toast.LENGTH_LONG).show();
                }
            }
        });

        ImageView plotItem = (ImageView) findViewById(R.id.action_plot_area);
        plotItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AreaDetailsActivity.this, AreaMapPlotterActivity.class);
                startActivity(intent);
            }
        });

        ImageView navigateItem = (ImageView) findViewById(R.id.action_navigate_area);
        navigateItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + ae.getCenterLat() + "," + ae.getCenterLon());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        ImageView shareAreaItem = (ImageView) findViewById(R.id.action_share_area);
        shareAreaItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.SHARE_READ_ONLY)) {
                    Intent areaShareIntent = new Intent(AreaDetailsActivity.this, AreaShareActivity.class);
                    startActivity(areaShareIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "You do not enough rights !!", Toast.LENGTH_LONG).show();
                }
            }
        });

        ImageView addResourcesItem = (ImageView) findViewById(R.id.action_drive_area);
        addResourcesItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.ADD_RESOURCES)) {
                    Intent intent = new Intent(AreaDetailsActivity.this, CreateFolderStructureActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "You do not enough rights !!", Toast.LENGTH_LONG).show();
                }
            }
        });

        ImageView displayResItem = (ImageView) findViewById(R.id.action_display_res);
        displayResItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AreaDetailsActivity.this, AreaDisplayResourcesActivity.class);
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
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Add some marker in the context saying that GPS is not enabled.
                    }
                })
                .show();
    }

    private boolean askForLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
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
        Intent areaDashboardIntent = new Intent(AreaDetailsActivity.this, AreaDashboardActivity.class);
        startActivity(areaDashboardIntent);
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean gps_enabled = false;

        if (locationManager == null)
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            //do nothing...
        }
        return gps_enabled;
    }

    private class DeleteAreaCallback implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            finish();
            Intent areaDashboardIntent = new Intent(AreaDetailsActivity.this, AreaDashboardActivity.class);
            startActivity(areaDashboardIntent);
        }
    }
}
