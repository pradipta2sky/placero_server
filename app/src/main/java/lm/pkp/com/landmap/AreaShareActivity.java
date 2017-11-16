package lm.pkp.com.landmap;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.permission.PermissionConstants;
import lm.pkp.com.landmap.permission.PermissionManager;
import lm.pkp.com.landmap.permission.PermissionsDBHelper;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserInfoSearchAsyncTask;
import lm.pkp.com.landmap.util.AreaPopulationUtil;
import lm.pkp.com.landmap.util.ColorProvider;
import lm.pkp.com.landmap.util.GeneralUtil;

public class AreaShareActivity extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private String targetUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(R.layout.activity_area_share);

        final AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setBackgroundDrawable(new ColorDrawable(ColorProvider.getAreaToolBarColor(areaElement)));
        ab.show();

        View includedView = findViewById(R.id.selected_area_include);
        AreaPopulationUtil.INSTANCE.populateAreaElement(includedView);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{});
        final AutoCompleteTextView userIdView = (AutoCompleteTextView) findViewById(R.id.user_search_text);
        userIdView.setAdapter(adapter);

        userIdView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                UserInfoSearchAsyncTask searcherTask = new UserInfoSearchAsyncTask();
                JSONObject searchParams = new JSONObject();
                try {
                    searchParams.put("ss", s.toString());
                    searchParams.put("sf", "name");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                searcherTask.setCompletionCallback(new UserInfoCallBack());
                searcherTask.execute(searchParams);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        RadioButton shareFullRadio = (RadioButton) findViewById(R.id.share_full_radio);
        if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.FULL_CONTROL)) {
            shareFullRadio.setEnabled(true);
        }

        RadioButton shareRestrictedRadio = (RadioButton) findViewById(R.id.share_restricted_radio);
        if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.SHARE_READ_WRITE)) {
            shareRestrictedRadio.setEnabled(true);
        }

        final RadioGroup roleGroup = (RadioGroup) findViewById(R.id.area_share_role_group);
        roleGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                ViewStub stub = (ViewStub) findViewById(R.id.share_details_stub);
                if (stub == null) {
                    // View stub is already inflated
                    View inflatedStub = findViewById(R.id.share_details_stub_restricted);
                    if (inflatedStub != null) {
                        if (inflatedStub.getVisibility() == View.VISIBLE) {
                            inflatedStub.setVisibility(View.GONE);
                        } else {
                            if (checkedId == R.id.share_restricted_radio) {
                                inflatedStub.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                } else {
                    if (checkedId == R.id.share_restricted_radio) {
                        stub.setLayoutResource(R.layout.area_share_restricted);
                        stub.inflate();
                    }
                }
            }
        });

        Button saveButton = (Button) findViewById(R.id.area_share_save_btn);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);

                final AutoCompleteTextView userIdView = (AutoCompleteTextView) findViewById(R.id.user_search_text);
                targetUser = userIdView.getText().toString();
                // target user should be a valid email.
                if (!GeneralUtil.isValidEmail(targetUser)) {
                    showErrorMessage("Please enter a valid email");
                    findViewById(R.id.splash_panel).setVisibility(View.GONE);
                    return;
                }

                int radioButtonID = roleGroup.getCheckedRadioButtonId();
                View radioButton = roleGroup.findViewById(radioButtonID);
                int idx = roleGroup.indexOfChild(radioButton);

                final PermissionsDBHelper pmh = new PermissionsDBHelper(getApplicationContext(),
                        new DatabaseUpdateCallback());
                if (idx == 0) {
                    // For view insert view_only permission.
                    pmh.insertPermissionsToServer(targetUser, "view_only");
                } else if (idx == 1) {
                    // For Full control insert full_control permission.
                    pmh.insertPermissionsToServer(targetUser, "full_control");
                } else if (idx == 2) {
                    View inflatedStub = findViewById(R.id.share_details_stub_restricted);
                    // For restricted read all the values.
                    ArrayList<View> touchableViews = inflatedStub.getTouchables();
                    List<String> checkedFunctions = new ArrayList<String>();
                    for (int i = 0; i < touchableViews.size(); i++) {
                        View actualView = touchableViews.get(i);
                        if (actualView instanceof CheckBox) {
                            final CheckBox checkBox = (CheckBox) actualView;
                            if (checkBox.isChecked()) {
                                checkedFunctions.add(checkBox.getTag().toString());
                            }
                        }
                    }
                    String joinedFunctions = TextUtils.join(",", checkedFunctions);
                    pmh.insertPermissionsToServer(targetUser, joinedFunctions);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private class UserInfoCallBack implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            try {
                String userArray = result.toString();
                String currUserEmail = UserContext.getInstance().getUserElement().getEmail();
                adapter.clear();

                if (!userArray.trim().equalsIgnoreCase("[]")) {
                    JSONArray responseArr = new JSONArray(userArray);
                    for (int i = 0; i < responseArr.length(); i++) {
                        JSONObject responseObj = (JSONObject) responseArr.get(i);
                        String emailStr = responseObj.getString("email");
                        if (!currUserEmail.equalsIgnoreCase(emailStr)) {
                            adapter.add(emailStr);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private class DatabaseUpdateCallback implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            Intent shareDriveResourcesIntent = new Intent(getApplicationContext(), ShareDriveResourcesActivity.class);
            shareDriveResourcesIntent.putExtra("share_to_user", targetUser);
            startActivity(shareDriveResourcesIntent);
            finish();
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
