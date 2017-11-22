package lm.pkp.com.landmap;

import android.Manifest.permission;
import android.R.string;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
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
import lm.pkp.com.landmap.weather.WeatherDisplayFragment;
import lm.pkp.com.landmap.weather.WeatherManager;
import lm.pkp.com.landmap.weather.model.WeatherElement;

public class AreaDetailsActivity extends AppCompatActivity implements LocationPositionReceiver {

    private PositionsDBHelper pdb;
    private AreaElement ae;

    private final ArrayList<PositionElement> positionList = new ArrayList<PositionElement>();
    private PostionListAdaptor adaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(layout.activity_area_details);
        getSupportActionBar().hide();

        ae = AreaContext.INSTANCE.getAreaElement();
        WeatherManager weatherManager = new WeatherManager(getApplicationContext(), new WeatherDataCallback());
        weatherManager.loadWeatherInfoForPosition(ae.getCenterPosition());

        Toolbar topTB = (Toolbar) findViewById(id.toolbar_top);
        ColorDrawable topDrawable = (ColorDrawable) topTB.getBackground().getCurrent();
        topDrawable.setColor(ColorProvider.getAreaToolBarColor(ae));

        Toolbar bottomTB = (Toolbar) findViewById(id.toolbar_bottom);
        ColorDrawable bottomDrawable = (ColorDrawable) bottomTB.getBackground().getCurrent();
        bottomDrawable.setColor(ColorProvider.getAreaToolBarColor(ae));

        if (!askForLocationPermission()) {
            showErrorMessage("No permission given for location fix !!", "error");
            finish();
        }

        pdb = new PositionsDBHelper(getApplicationContext());
        ListView posListView = (ListView) findViewById(id.positionList);
        positionList.addAll(ae.getPositions());

        adaptor = new PostionListAdaptor(getApplicationContext(), id.positionList, positionList);
        posListView.setAdapter(adaptor);
        adaptor.notifyDataSetChanged();

        TextView areaNameView = (TextView) findViewById(id.area_name_text);
        String areaName = ae.getName();
        if (areaName.length() > 16) {
            areaName = areaName.substring(0, 15).concat("...");
        }
        areaNameView.setText(areaName);

        findViewById(id.splash_panel).setVisibility(View.GONE);
        if (positionList.size() == 0) {
            findViewById(id.positions_view_master).setVisibility(View.GONE);
            findViewById(id.position_list_empty_img).setVisibility(View.VISIBLE);
        } else {
            findViewById(id.positions_view_master).setVisibility(View.VISIBLE);
            findViewById(id.position_list_empty_img).setVisibility(View.GONE);
            // Render the weather data here.

        }

        ImageView areaEditItem = (ImageView) findViewById(id.action_area_edit);
        areaEditItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent areaEditIntent = new Intent(getApplicationContext(), AreaEditActivity.class);
                startActivity(areaEditIntent);
            }
        });

        ImageView deleteAreaItem = (ImageView) findViewById(id.action_delete_area);
        deleteAreaItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.REMOVE_AREA)) {
                    AreaDBHelper adh = new AreaDBHelper(getApplicationContext(), new DeleteAreaCallback());
                    adh.deleteArea(ae);
                    adh.deleteAreaFromServer(ae);
                } else {
                    showErrorMessage("You do not have removal rights !!", "error");
                }
            }
        });


        OnClickListener markLocationOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.MARK_POSITION)) {
                    findViewById(id.position_list_empty_img).setVisibility(View.GONE);
                    findViewById(id.positions_view_master).setVisibility(View.GONE);
                    findViewById(id.splash_panel).setVisibility(View.VISIBLE);
                    new GPSLocationProvider(AreaDetailsActivity.this).getLocation();
                } else {
                    showErrorMessage("You do not have Plotting rights !!", "error");
                }
            }
        };

        ImageView markLocationItem = (ImageView) findViewById(id.action_mark_location);
        markLocationItem.setOnClickListener(markLocationOnClickListener);

        ImageView markLocationEmptyItem = (ImageView) findViewById(id.action_mark_location_empty);
        if (markLocationEmptyItem != null) {
            markLocationEmptyItem.setOnClickListener(markLocationOnClickListener);
        }

        ImageView plotItem = (ImageView) findViewById(id.action_plot_area);
        plotItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                List<PositionElement> positions = ae.getPositions();
                if (positions.size() >= 1) {
                    Intent intent = new Intent(getApplicationContext(), AreaMapPlotterActivity.class);
                    startActivity(intent);
                } else {
                    showErrorMessage("You need atleast 1 points to plot!!!", "error");
                }
            }
        });

        ImageView navigateItem = (ImageView) findViewById(id.action_navigate_area);
        navigateItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                List<PositionElement> positions = ae.getPositions();
                if (positions.size() > 0) {
                    PositionElement pe = positions.get(0);
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + pe.getLat() + "," + pe.getLon());
                    Intent navigationIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    navigationIntent.setPackage("com.google.android.apps.maps");
                    startActivity(navigationIntent);
                } else {
                    showErrorMessage("No positions available for navigation", "error");
                }
            }
        });

        ImageView shareAreaItem = (ImageView) findViewById(id.action_share_area);
        shareAreaItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.SHARE_READ_ONLY)) {
                    Intent areaShareIntent = new Intent(getApplicationContext(), AreaShareActivity.class);
                    startActivity(areaShareIntent);
                } else {
                    showErrorMessage("You do not have area sharing rights !!", "error");
                }
            }
        });

        ImageView addResourcesItem = (ImageView) findViewById(id.action_drive_area);
        addResourcesItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.ADD_RESOURCES)) {
                    Intent intent = new Intent(getApplicationContext(), AreaAddResourcesActivity.class);
                    startActivity(intent);
                } else {
                    showErrorMessage("You do not have resource addition rights !!", "error");
                }
            }
        });

        ImageView displayResItem = (ImageView) findViewById(id.action_display_res);
        displayResItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DownloadDriveResourcesActivity.class);
                startActivity(intent);
            }
        });

        showErrorsIfAny();
    }

    private void showErrorsIfAny() {
        Bundle intentBundle = getIntent().getExtras();
        if (intentBundle != null) {
            String action = intentBundle.getString("action");
            String outcome = intentBundle.getString("outcome");
            String outcomeType = intentBundle.getString("outcome_type");
            showErrorMessage(action + " " + outcomeType + ". " + outcome, outcomeType);
        }
    }

    @Override
    public void receivedLocationPostion(PositionElement pe) {
        pe.setName("P_" + UUID.randomUUID());
        pe.setUniqueAreaId(ae.getUniqueId());

        AreaElement ae = AreaContext.INSTANCE.getAreaElement();
        ae.getPositions().add(pe);

        pe = pdb.insertPositionLocally(pe);
        pdb.insertPositionToServer(pe);

        AreaContext.INSTANCE.setAreaElement(ae, getApplicationContext());
        if (ae.getPositions().size() > 0) {
            WeatherManager weatherManager = new WeatherManager(getApplicationContext(), new WeatherDataCallback());
            weatherManager.loadWeatherInfoForPosition(ae.getCenterPosition());
        }

        findViewById(id.positions_view_master).setVisibility(View.VISIBLE);
        findViewById(id.position_list_empty_img).setVisibility(View.GONE);
        findViewById(id.splash_panel).setVisibility(View.GONE);

        positionList.add(pe);
        adaptor.notifyDataSetChanged();
    }

    @Override
    public void locationFixTimedOut() {
        findViewById(id.splash_panel).setVisibility(View.GONE);
        if (positionList.size() == 0) {
            findViewById(id.positions_view_master).setVisibility(View.GONE);
            findViewById(id.position_list_empty_img).setVisibility(View.VISIBLE);
        } else {
            findViewById(id.positions_view_master).setVisibility(View.VISIBLE);
            findViewById(id.position_list_empty_img).setVisibility(View.GONE);
        }
        showLocationFixFailureDialog();
    }

    @Override
    public void providerDisabled() {
        findViewById(id.splash_panel).setVisibility(View.GONE);
        if (positionList.size() == 0) {
            findViewById(id.positions_view_master).setVisibility(View.GONE);
            findViewById(id.position_list_empty_img).setVisibility(View.VISIBLE);
        } else {
            findViewById(id.position_list_empty_img).setVisibility(View.GONE);
            findViewById(id.positions_view_master).setVisibility(View.VISIBLE);
        }
        showEnableGPSDialog();
    }

    @Override
    public void onBackPressed() {
        Intent areaDashboardIntent = new Intent(getApplicationContext(), AreaDashboardActivity.class);
        startActivity(areaDashboardIntent);
        finish();
    }

    private boolean askForLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    permission.ACCESS_FINE_LOCATION,
                    permission.ACCESS_COARSE_LOCATION}, 1);
            return ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED;
        }
    }

    private void showEnableGPSDialog() {
        new Builder(this)
                .setTitle("GPS Location disabled.")
                .setMessage("Do you want to enable GPS ?")
                .setPositiveButton(string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Add some marker in the context saying that GPS is not enabled.
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton(string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Add some marker in the context saying that GPS is not enabled.
                    }
                })
                .show();
    }

    private void showLocationFixFailureDialog() {
        new Builder(this)
                .setTitle("Location Fix failed. Please stay outdoors !!")
                .setMessage("Do you want to try again ?")
                .setPositiveButton(string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        findViewById(id.positions_view_master).setVisibility(View.GONE);
                        findViewById(id.position_list_empty_img).setVisibility(View.GONE);
                        findViewById(id.splash_panel).setVisibility(View.VISIBLE);
                        new GPSLocationProvider(AreaDetailsActivity.this).getLocation();
                    }
                })
                .setNegativeButton(string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Add some marker in the context saying that GPS is not enabled.
                    }
                })
                .show();
    }

    private void showErrorMessage(String message, String type) {
        final Snackbar snackbar = Snackbar.make(getWindow().getDecorView(),
                message + ".", Snackbar.LENGTH_INDEFINITE);

        View sbView = snackbar.getView();
        snackbar.getView().setBackgroundColor(Color.parseColor("#FAF7F6"));

        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        if (type.equalsIgnoreCase("info")) {
            textView.setTextColor(Color.parseColor("#30601F"));
        } else if (type.equalsIgnoreCase("error")) {
            textView.setTextColor(Color.RED);
        } else {
            textView.setTextColor(Color.DKGRAY);
        }
        textView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
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

    private class DeleteAreaCallback implements AsyncTaskCallback {
        @Override
        public void taskCompleted(Object result) {
            finish();
            Intent areaDashboardIntent = new Intent(getApplicationContext(), RemoveDriveResourcesActivity.class);
            startActivity(areaDashboardIntent);
        }
    }

    private class WeatherDataCallback implements AsyncTaskCallback {
        @Override
        public void taskCompleted(Object result) {
            if (result instanceof WeatherElement) {
                getSupportFragmentManager().beginTransaction()
                        .add(id.weather_container, new WeatherDisplayFragment())
                        .commit();
            }
        }
    }

}
