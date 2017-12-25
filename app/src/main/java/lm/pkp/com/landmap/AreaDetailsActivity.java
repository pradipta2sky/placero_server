package lm.pkp.com.landmap;

import android.Manifest.permission;
import android.R.string;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.custom.GlobalContext;
import lm.pkp.com.landmap.custom.LocationPositionReceiver;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.permission.PermissionConstants;
import lm.pkp.com.landmap.permission.PermissionManager;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.position.PositionListAdaptor;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.provider.GPSLocationProvider;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserElement;
import lm.pkp.com.landmap.util.ColorProvider;
import lm.pkp.com.landmap.weather.WeatherDisplayFragment;
import lm.pkp.com.landmap.weather.WeatherManager;
import lm.pkp.com.landmap.weather.model.WeatherElement;

public class AreaDetailsActivity extends AppCompatActivity implements LocationPositionReceiver {

    private PositionsDBHelper pdb;
    private AreaElement ae;
    private boolean online = true;

    private final ArrayList<PositionElement> positionList = new ArrayList<PositionElement>();
    private PositionListAdaptor adaptor;

    @Override
    public void showLockTaskEscapeMessage() {
        super.showLockTaskEscapeMessage();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);
        online = new Boolean(GlobalContext.INSTANCE.get(GlobalContext.INTERNET_AVAILABLE));

        setContentView(R.layout.activity_area_details);
        getSupportActionBar().hide();

        ae = AreaContext.INSTANCE.getAreaElement();

        Toolbar topTB = (Toolbar) findViewById(R.id.toolbar_top);
        ColorDrawable topDrawable = (ColorDrawable) topTB.getBackground().getCurrent();
        topDrawable.setColor(ColorProvider.getAreaToolBarColor(ae));

        Toolbar bottomTB = (Toolbar) findViewById(R.id.toolbar_bottom);
        ColorDrawable bottomDrawable = (ColorDrawable) bottomTB.getBackground().getCurrent();
        bottomDrawable.setColor(ColorProvider.getAreaToolBarColor(ae));

        if (!askForLocationPermission()) {
            showMessage("No permission given for location fix !!", "error");
            finish();
        }

        pdb = new PositionsDBHelper(getApplicationContext());
        ListView posListView = (ListView) findViewById(R.id.positionList);
        positionList.addAll(ae.getPositions());

        adaptor = new PositionListAdaptor(this, id.positionList, positionList);
        posListView.setAdapter(adaptor);
        adaptor.notifyDataSetChanged();

        TextView areaNameView = (TextView) findViewById(R.id.area_name_text);
        String areaName = ae.getName();
        if (areaName.length() > 16) {
            areaName = areaName.substring(0, 15).concat("...");
        }
        areaNameView.setText(areaName);

        findViewById(R.id.splash_panel).setVisibility(View.GONE);
        if (positionList.size() == 0) {
            findViewById(R.id.positions_view_master).setVisibility(View.GONE);
            findViewById(R.id.position_list_empty_img).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.positions_view_master).setVisibility(View.VISIBLE);
            findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
        }

        ImageView areaEditItem = (ImageView) findViewById(R.id.action_area_edit);
        areaEditItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.CHANGE_NAME)
                        && PermissionManager.INSTANCE.hasAccess(PermissionConstants.CHANGE_DESCRIPTION)) {
                    Intent areaEditIntent = new Intent(getApplicationContext(), AreaEditActivity.class);
                    startActivity(areaEditIntent);
                }else {
                    showMessage("You do not have change rights !!", "error");
                }
            }
        });

        ImageView deleteAreaItem = (ImageView) findViewById(R.id.action_delete_area);
        deleteAreaItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.REMOVE_AREA)) {
                    showAreaDeleteConfirmation();
                } else {
                    showMessage("You do not have removal rights !!", "error");
                }
            }
        });

        ImageView markLocationItem = (ImageView) findViewById(R.id.action_mark_location);
        markLocationItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.MARK_POSITION)) {
                    findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
                    findViewById(R.id.positions_view_master).setVisibility(View.GONE);
                    findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
                    new GPSLocationProvider(AreaDetailsActivity.this).getLocation();
                } else {
                    showMessage("You do not have Plotting rights !!", "error");
                }
            }
        });

        ImageView markLocationEmptyItem = (ImageView) findViewById(R.id.action_mark_location_empty);
        if (markLocationEmptyItem != null) {
            markLocationEmptyItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.MARK_POSITION)) {
                        findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
                        findViewById(R.id.positions_view_master).setVisibility(View.GONE);
                        findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
                        new GPSLocationProvider(AreaDetailsActivity.this).getLocation();
                    } else {
                        showMessage("You do not have Plotting rights !!", "error");
                    }
                }
            });
        }

        ImageView plotItem = (ImageView) findViewById(R.id.action_plot_area);
        plotItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!online){
                    showMessage("No Internet..", "error");
                    return;
                }
                List<PositionElement> positions = ae.getPositions();
                if (positions.size() >= 1) {
                    Intent intent = new Intent(getApplicationContext(), AreaMapPlotterActivity.class);
                    startActivity(intent);
                } else {
                    showMessage("You need atleast 1 points to plot!!!", "error");
                }
            }
        });

        ImageView navigateItem = (ImageView) findViewById(R.id.action_navigate_area);
        navigateItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!online){
                    showMessage("No Internet..", "error");
                    return;
                }
                List<PositionElement> positions = ae.getPositions();
                if (positions.size() > 0) {
                    UserElement userElement = UserContext.getInstance().getUserElement();
                    PositionElement position = userElement.getSelections().getPosition();
                    if(position == null){
                        position = positions.get(0);
                    }
                    Uri gmmIntentUri = Uri.parse("google.navigation:q="
                            + position.getLat() + "," + position.getLon()+"&daddr=" + ae.getName());
                    Intent navigationIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    navigationIntent.setPackage("com.google.android.apps.maps");
                    startActivity(navigationIntent);
                } else {
                    showMessage("No positions available for navigation", "error");
                }
            }
        });

        ImageView shareAreaItem = (ImageView) findViewById(R.id.action_share_area);
        shareAreaItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!online){
                    showMessage("No Internet..", "error");
                    return;
                }
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.SHARE_READ_ONLY)) {
                    Intent areaShareIntent = new Intent(getApplicationContext(), AreaShareActivity.class);
                    startActivity(areaShareIntent);
                } else {
                    showMessage("You do not have area sharing rights !!", "error");
                }
            }
        });

        ImageView addResourcesItem = (ImageView) findViewById(R.id.action_drive_area);
        addResourcesItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.ADD_RESOURCES)) {
                    Intent intent = new Intent(getApplicationContext(), AreaAddResourcesActivity.class);
                    startActivity(intent);
                } else {
                    showMessage("You do not have resource addition rights !!", "error");
                }
            }
        });

        ImageView displayResItem = (ImageView) findViewById(R.id.action_display_res);
        displayResItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(online){
                    Intent intent = new Intent(getApplicationContext(), DownloadDriveResourcesActivity.class);
                    startActivity(intent);
                }else {
                    Intent displayIntent = new Intent(getApplicationContext(), AreaResourceDisplayActivity.class);
                    startActivity(displayIntent);
                    finish();
                }
            }
        });

        ImageView weatherItem = (ImageView) findViewById(R.id.action_area_weather);
        weatherItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(online){
                    WeatherManager weatherManager = new WeatherManager(getApplicationContext(),
                            new WeatherDataCallback());
                    weatherManager.loadWeatherInfoForPosition(ae.getCenterPosition());
                }else {
                    showMessage("Internet unavailable", "error");
                }
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
            showMessage(action + " " + outcomeType + ". " + outcome, outcomeType);
        }
    }

    @Override
    public void receivedLocationPostion(PositionElement pe) {
        pe.setName("P_" + UUID.randomUUID());
        pe.setUniqueAreaId(ae.getUniqueId());

        AreaElement ae = AreaContext.INSTANCE.getAreaElement();
        List<PositionElement> positions = ae.getPositions();
        if(!positions.contains(pe)){
            pe.setName("Position_" + positions.size());
            positions.add(pe);
            pdb.insertPositionLocally(pe);
            pdb.insertPositionToServer(pe);

            AreaContext.INSTANCE.setAreaElement(ae, getApplicationContext());
            positionList.add(pe);
            adaptor.notifyDataSetChanged();
        }else{
            showMessage("Position already exists. Ignoring.", "info");
        }

        if (positions.size() > 0) {
            WeatherManager weatherManager = new WeatherManager(getApplicationContext(), new WeatherDataCallback());
            weatherManager.loadWeatherInfoForPosition(ae.getCenterPosition());
        }
        findViewById(R.id.positions_view_master).setVisibility(View.VISIBLE);
        findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
        findViewById(R.id.splash_panel).setVisibility(View.GONE);

        showPositionEdit(pe);
    }

    @Override
    public void locationFixTimedOut() {
        findViewById(R.id.splash_panel).setVisibility(View.GONE);
        if (positionList.size() == 0) {
            findViewById(R.id.positions_view_master).setVisibility(View.GONE);
            findViewById(R.id.position_list_empty_img).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.positions_view_master).setVisibility(View.VISIBLE);
            findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
        }
        showLocationFixFailureDialog();
    }

    @Override
    public void providerDisabled() {
        findViewById(R.id.splash_panel).setVisibility(View.GONE);
        if (positionList.size() == 0) {
            findViewById(R.id.positions_view_master).setVisibility(View.GONE);
            findViewById(R.id.position_list_empty_img).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
            findViewById(R.id.positions_view_master).setVisibility(View.VISIBLE);
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
                        findViewById(R.id.positions_view_master).setVisibility(View.GONE);
                        findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
                        findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
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

    private void showAreaDeleteConfirmation() {
        new Builder(this)
                .setTitle("Delete Area !!, Cannot Undo")
                .setMessage("Do you really want to remove the area ?")
                .setPositiveButton(string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        AreaDBHelper adh = new AreaDBHelper(getApplicationContext(), new DeleteAreaCallback());
                        adh.deleteArea(ae);
                        adh.deleteAreaFromServer(ae);
                    }
                })
                .setNegativeButton(string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Add some marker in the context saying that GPS is not enabled.
                    }
                })
                .show();
    }

    private class DeleteAreaCallback implements AsyncTaskCallback {
        @Override
        public void taskCompleted(Object result) {
            finish();
            Intent areaDashboardIntent = new Intent(getApplicationContext(), RemoveDriveResourcesActivity.class);
            startActivity(areaDashboardIntent);
        }
    }

    public void showPositionEdit(final PositionElement positionElement){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Position Details");

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.position_edit,
                (ViewGroup) findViewById(R.id.position_edit_layout_root), false);
        builder.setView(v);

        final EditText posNameView = (EditText) v.findViewById(R.id.position_name);
        posNameView.setText(positionElement.getName());

        final EditText posDescView = (EditText) v.findViewById(R.id.position_desc);
        posDescView.setText(positionElement.getDescription());

        final Spinner spinner = (Spinner) v.findViewById(id.position_type);
        String ptype = StringUtils.capitalize(positionElement.getType());
        spinner.setSelection(((ArrayAdapter)spinner.getAdapter()).getPosition(ptype));

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                positionElement.setName(posNameView.getText().toString());
                positionElement.setType(spinner.getSelectedItem().toString());
                positionElement.setDescription(posDescView.getText().toString());

                PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
                pdh.updatePositionLocally(positionElement);
                pdh.updatePositionToServer(positionElement);

                adaptor.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showMessage(String message, String type) {
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

    private class WeatherDataCallback implements AsyncTaskCallback {
        @Override
        public void taskCompleted(Object result) {
            if (result instanceof WeatherElement) {
                showWeather();
            }
        }
    }

    public void showWeather(){
        DialogFragment dFragment = new WeatherDisplayFragment();
        dFragment.show(getSupportFragmentManager(), "Weather Now");
    }

}
