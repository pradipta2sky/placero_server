package lm.pkp.com.landmap;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.R.id;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.model.AreaAddress;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.connectivity.ConnectivityChangeReceiver;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.custom.GlobalContext;
import lm.pkp.com.landmap.permission.PermissionConstants;
import lm.pkp.com.landmap.permission.PermissionManager;
import lm.pkp.com.landmap.permission.PermissionsDBHelper;
import lm.pkp.com.landmap.util.AreaPopulationUtil;
import lm.pkp.com.landmap.util.ColorProvider;

public class AreaEditActivity extends AppCompatActivity {

    private boolean online = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);
        online = ConnectivityChangeReceiver.isConnected(this);
        setContentView(R.layout.activity_area_edit);

        final AreaElement ae = AreaContext.INSTANCE.getAreaElement();
        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(false);
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setBackgroundDrawable(new ColorDrawable(ColorProvider.getAreaToolBarColor(ae)));
        ab.show();

        View includedView = findViewById(R.id.selected_area_include);
        AreaPopulationUtil.INSTANCE.populateAreaElement(includedView);

        final TextView nameText = (TextView) findViewById(R.id.area_name_edit);
        String areaName = ae.getName();
        if (areaName.length() > 20) {
            areaName = areaName.substring(0, 19).concat("...");
        }
        nameText.setText(areaName);
        if (!PermissionManager.INSTANCE.hasAccess(PermissionConstants.CHANGE_NAME)) {
            nameText.setEnabled(false);
        }

        final TextView descText = (TextView) findViewById(R.id.area_desc_edit);
        descText.setText(ae.getDescription());
        if (!PermissionManager.INSTANCE.hasAccess(PermissionConstants.CHANGE_DESCRIPTION)) {
            descText.setEnabled(false);
        }

        final TextView addressText = (TextView) findViewById(R.id.area_address_edit);
        AreaAddress address = ae.getAddress();
        if(address != null){
            addressText.setText(address.getDisplaybleAddress());
        }else {
            addressText.setText("");
        }
        addressText.setEnabled(false);

        CheckBox makePublicCheckBox = (CheckBox) findViewById(R.id.make_area_public);
        if(!online){
            makePublicCheckBox.setEnabled(false);
        }

        Button saveButton = (Button) findViewById(R.id.area_edit_save_btn);
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);

                String areaName = nameText.getText().toString();
                if (areaName.trim().equalsIgnoreCase("")) {
                    showErrorMessage("Area Name is required !!");
                    findViewById(R.id.splash_panel).setVisibility(View.GONE);
                    return;
                }
                ae.setName(areaName);
                ae.setDescription(descText.getText().toString());

                AreaDBHelper adh = new AreaDBHelper(getApplicationContext(), new UpdateAreaToServerCallback());
                adh.updateAreaLocally(ae);
                if(!adh.updateAreaOnServer(ae)){
                    if(makeAreaPublic()){
                        navigateToDetailsView();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent areaDetails = new Intent(this.getApplicationContext(), AreaDetailsActivity.class);
        this.startActivity(areaDetails);
        this.finish();
    }

    private class UpdateAreaToServerCallback implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            makeAreaPublic();
        }
    }

    private boolean makeAreaPublic() {
        CheckBox makePublicCheckBox = (CheckBox) findViewById(R.id.make_area_public);
        if (makePublicCheckBox.isChecked()) {
            PermissionsDBHelper pdh = new PermissionsDBHelper(getApplicationContext(),
                    new MakeAreaPublicCallback());
            pdh.insertPermissionsToServer("any", "view_only");
            return true;
        } else {
            navigateToDetailsView();
            return false;
        }
    }

    private void navigateToDetailsView() {
        findViewById(R.id.splash_panel).setVisibility(View.INVISIBLE);
        Intent areaDetailsIntent = new Intent(getApplicationContext(), AreaDetailsActivity.class);
        startActivity(areaDetailsIntent);
        finish();
    }

    private class MakeAreaPublicCallback implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            if(online){
                Intent areaDetailsIntent = new Intent(AreaEditActivity.this, ShareDriveResourcesActivity.class);
                areaDetailsIntent.putExtra("share_to_user", "any");
                startActivity(areaDetailsIntent);
                finish();
            }
        }
    }

    private void showErrorMessage(String message) {
        Snackbar snackbar = Snackbar.make(this.getWindow().getDecorView(), message, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }

}
