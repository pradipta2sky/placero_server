package lm.pkp.com.landmap;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.permission.PermissionConstants;
import lm.pkp.com.landmap.permission.PermissionManager;
import lm.pkp.com.landmap.permission.PermissionsDBHelper;
import lm.pkp.com.landmap.util.AreaPopulationUtil;
import lm.pkp.com.landmap.util.ColorProvider;

public class AreaEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

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
        addressText.setText(ae.getAddress());
        if (!PermissionManager.INSTANCE.hasAccess(PermissionConstants.CHANGE_ADDRESS)) {
            addressText.setEnabled(false);
        }

        Button saveButton = (Button) findViewById(R.id.area_edit_save_btn);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);

                String areaName = nameText.getText().toString();
                if (areaName.trim().equalsIgnoreCase("")) {
                    showErrorMessage("User selection is required for share !!");
                    findViewById(R.id.splash_panel).setVisibility(View.GONE);
                    return;
                }
                ae.setName(areaName);
                ae.setDescription(descText.getText().toString());
                ae.setAddress(addressText.getText().toString());

                AreaDBHelper adh = new AreaDBHelper(getApplicationContext(), new UpdateAreaToServerCallback());
                adh.updateAreaAttributes(ae);
                adh.updateAreaOnServer(ae);
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

    private class UpdateAreaToServerCallback implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            // Work on the make public option.
            final CheckBox makePublicCheckBox = (CheckBox) findViewById(R.id.make_area_public);
            if (makePublicCheckBox.isChecked()) {
                PermissionsDBHelper pdh = new PermissionsDBHelper(getApplicationContext(), new MakeAreaPublicCallback());
                pdh.insertPermissionsToServer("any", "view_only");
            } else {
                findViewById(R.id.splash_panel).setVisibility(View.INVISIBLE);
                finish();
                Intent areaDetailsIntent = new Intent(AreaEditActivity.this, AreaDetailsActivity.class);
                startActivity(areaDetailsIntent);
            }
        }
    }

    private class MakeAreaPublicCallback implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            findViewById(R.id.splash_panel).setVisibility(View.INVISIBLE);
            finish();
            Intent areaDetailsIntent = new Intent(AreaEditActivity.this, AreaDetailsActivity.class);
            startActivity(areaDetailsIntent);
        }
    }

    private void showErrorMessage(String message) {
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView(), message, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }

}
