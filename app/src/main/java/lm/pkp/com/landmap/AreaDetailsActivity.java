package lm.pkp.com.landmap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
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

        ae = AreaContext.INSTANCE.getAreaElement();
        Toolbar topTB = (Toolbar) findViewById(R.id.toolbar_top);
        final ColorDrawable topDrawable = (ColorDrawable) topTB.getBackground().getCurrent();
        topDrawable.setColor(ColorProvider.getAreaToolBarColor(ae));

        Toolbar bottomTB = (Toolbar) findViewById(R.id.toolbar_bottom);
        final ColorDrawable bottomDrawable = (ColorDrawable) bottomTB.getBackground().getCurrent();
        bottomDrawable.setColor(ColorProvider.getAreaToolBarColor(ae));

        if (!askForLocationPermission()) {
            showErrorMessage("No permission given for location fix !!");
            finish();
        }

        pdb = new PositionsDBHelper(getApplicationContext());
        ListView posListView = (ListView) findViewById(R.id.positionList);
        positionList.addAll(ae.getPositions());

        adaptor = new PostionListAdaptor(getApplicationContext(), R.id.positionList, positionList);
        posListView.setAdapter(adaptor);
        adaptor.notifyDataSetChanged();

        TextView areaNameView = (TextView) findViewById(R.id.area_name_text);
        String areaName = ae.getName();
        if (areaName.length() > 20) {
            areaName = areaName.substring(0, 19).concat("...");
        }
        areaNameView.setText(areaName);

        if(positionList.size() == 0){
            findViewById(R.id.position_list_scroll).setVisibility(View.GONE);
            findViewById(R.id.position_list_empty_img).setVisibility(View.VISIBLE);
        }else {
            findViewById(R.id.position_list_scroll).setVisibility(View.VISIBLE);
            findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
        }

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
                    final AreaDBHelper adh = new AreaDBHelper(getApplicationContext(), new DeleteAreaCallback());
                    adh.deleteArea(ae);
                    adh.deleteAreaFromServer(ae);
                } else {
                    showErrorMessage("You do not have removal rights !!");
                }
            }
        });

        ImageView markLocationItem = (ImageView) findViewById(R.id.action_mark_location);
        markLocationItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.MARK_POSITION)) {
                    findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
                    findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
                    new GPSLocationProvider(AreaDetailsActivity.this).getLocation();
                } else {
                    showErrorMessage("You do not have Plotting rights !!");
                }
            }
        });

        ImageView plotItem = (ImageView) findViewById(R.id.action_plot_area);
        plotItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<PositionElement> positions = ae.getPositions();
                if (positions.size() >= 3) {
                    Intent intent = new Intent(AreaDetailsActivity.this, AreaMapPlotterActivity.class);
                    startActivity(intent);
                } else {
                    showErrorMessage("You need atleast 3 points to plot.!!!");
                }
            }
        });

        ImageView navigateItem = (ImageView) findViewById(R.id.action_navigate_area);
        navigateItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final double centerLat = ae.getCenterLat();
                final double centerLon = ae.getCenterLon();
                if (centerLat == 0 && centerLon == 0) {
                    final List<PositionElement> positions = ae.getPositions();
                    if (positions.size() > 0) {
                        final PositionElement pe = positions.get(0);
                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + pe.getLat() + "," + pe.getLon());
                        Intent navigationIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        navigationIntent.setPackage("com.google.android.apps.maps");
                        startActivity(navigationIntent);
                    } else {
                        showErrorMessage("No locations available for navigation");
                    }
                } else {
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + centerLat + "," + centerLon);
                    Intent navigationIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    navigationIntent.setPackage("com.google.android.apps.maps");
                    startActivity(navigationIntent);
                }
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
                    showErrorMessage("You do not have area sharing rights !!");
                }
            }
        });

        ImageView addResourcesItem = (ImageView) findViewById(R.id.action_drive_area);
        addResourcesItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.ADD_RESOURCES)) {
                    Intent intent = new Intent(AreaDetailsActivity.this, AreaAddResourcesActivity.class);
                    startActivity(intent);
                } else {
                    showErrorMessage("You do not have resource modification rights !!");
                }
            }
        });

        ImageView displayResItem = (ImageView) findViewById(R.id.action_display_res);
        displayResItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AreaDetailsActivity.this, DownloadDriveResourcesActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void receivedLocationPostion(PositionElement pe) {
        pe.setName("P_" + UUID.randomUUID().toString());
        pe.setUniqueAreaId(ae.getUniqueId());

        final AreaElement ae = AreaContext.INSTANCE.getAreaElement();
        ae.getPositions().add(pe);

        pe = pdb.insertPositionLocally(pe);
        pdb.insertPositionToServer(pe);

        findViewById(R.id.position_list_scroll).setVisibility(View.VISIBLE);
        findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
        findViewById(R.id.splash_panel).setVisibility(View.GONE);

        positionList.add(pe);
        adaptor.notifyDataSetChanged();
    }

    @Override
    public void locationFixTimedOut() {
        if(positionList.size() == 0){
            findViewById(R.id.position_list_scroll).setVisibility(View.GONE);
            findViewById(R.id.position_list_empty_img).setVisibility(View.VISIBLE);
            findViewById(R.id.splash_panel).setVisibility(View.GONE);
        }else {
            findViewById(R.id.position_list_scroll).setVisibility(View.VISIBLE);
            findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
            findViewById(R.id.splash_panel).setVisibility(View.GONE);
        }
        showLocationFixFailureDialog();
    }

    @Override
    public void providerDisabled() {
        if(positionList.size() == 0){
            findViewById(R.id.position_list_scroll).setVisibility(View.GONE);
            findViewById(R.id.position_list_empty_img).setVisibility(View.VISIBLE);
            findViewById(R.id.splash_panel).setVisibility(View.GONE);
        }else {
            findViewById(R.id.position_list_scroll).setVisibility(View.VISIBLE);
            findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
            findViewById(R.id.splash_panel).setVisibility(View.GONE);
        }
        showEnableGPSDialog();
    }

    @Override
    public void onBackPressed() {
        Intent areaDashboardIntent = new Intent(getApplicationContext(), AreaDashboardActivity.class);
        startActivity(areaDashboardIntent);
        finish();
    }


     private class DeleteAreaCallback implements AsyncTaskCallback {
        @Override
        public void taskCompleted(Object result) {
            finish();

            Intent areaDashboardIntent = new Intent(AreaDetailsActivity.this, RemoveDriveResourcesActivity.class);
            startActivity(areaDashboardIntent);
        }
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

    private void showEnableGPSDialog() {
        new AlertDialog.Builder(this)
                .setTitle("GPS Location disabled.")
                .setMessage("Do you want to enable GPS ?")
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

    private void showLocationFixFailureDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Location Fix failed. Please stay outdoors !!")
                .setMessage("Do you want to try again ?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        findViewById(R.id.position_list_scroll).setVisibility(View.GONE);
                        findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
                        findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
                        new GPSLocationProvider(AreaDetailsActivity.this).getLocation();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Add some marker in the context saying that GPS is not enabled.
                    }
                })
                .show();
    }

    private void showErrorMessage(String message) {
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView(), message, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }


}
