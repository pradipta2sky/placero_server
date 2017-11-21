package lm.pkp.com.landmap;

import android.Manifest;
import android.Manifest.permission;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Builder;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.Permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.drive.DriveResource;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks;

public class ShareDriveResourcesActivity extends Activity implements PermissionCallbacks {

    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {DriveScopes.DRIVE_METADATA_READONLY};
    private String shareToUser = "";
    private final String shareRole = "reader";
    private String shareType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        this.setContentView(layout.activity_share_area_resources);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            this.shareToUser = extras.getString("share_to_user");
            if (this.shareToUser.equalsIgnoreCase("any")) {
                this.shareType = "anyone";
            } else {
                this.shareType = "user";
            }
        }

        this.mProgress = new ProgressDialog(this);
        this.mProgress.setMessage("Sharing Area ...");
        this.mCredential = GoogleAccountCredential.usingOAuth2(this.getApplicationContext(), Arrays.asList(ShareDriveResourcesActivity.SCOPES)).setBackOff(new ExponentialBackOff());
        this.shareResources();
    }


    private void shareResources() {
        if (!this.isGooglePlayServicesAvailable()) {
            this.acquireGooglePlayServices();
        } else if (this.mCredential.getSelectedAccountName() == null) {
            this.chooseAccount();
        } else {
            new MakeRequestTask(this.mCredential).execute();
        }
    }

    @AfterPermissionGranted(ShareDriveResourcesActivity.REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, permission.GET_ACCOUNTS)) {
            String accountName = this.getPreferences(Context.MODE_PRIVATE)
                    .getString(ShareDriveResourcesActivity.PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                this.mCredential.setSelectedAccountName(accountName);
                this.shareResources();
            } else {
                // Start a dialog from which the user can choose an account
                this.startActivityForResult(this.mCredential.newChooseAccountIntent(), ShareDriveResourcesActivity.REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    ShareDriveResourcesActivity.REQUEST_PERMISSION_GET_ACCOUNTS,
                    permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ShareDriveResourcesActivity.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    this.shareResources();
                } else {
                    // TODO Share the error with the user
                }
                break;
            case ShareDriveResourcesActivity.REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = this.getPreferences(Context.MODE_PRIVATE);
                        Editor editor = settings.edit();
                        editor.putString(ShareDriveResourcesActivity.PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        this.mCredential.setSelectedAccountName(accountName);
                        this.shareResources();
                    }
                }
                break;
            case ShareDriveResourcesActivity.REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    this.shareResources();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            this.showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    void showGooglePlayServicesAvailabilityErrorDialog(
            int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(this, connectionStatusCode, ShareDriveResourcesActivity.REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private Drive mService;
        private Exception mLastError;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            this.mService = new Builder(transport, jsonFactory, credential).setApplicationName("LMS").build();
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                final List<String> fileInfo = new ArrayList<String>();
                List<DriveResource> drs = AreaContext.INSTANCE.getAreaElement().getMediaResources();
                for (final DriveResource dr : drs) {
                    BatchRequest batch = this.mService.batch();

                    Permission userPermission = new Permission();
                    userPermission.setType(ShareDriveResourcesActivity.this.shareType);
                    userPermission.setRole(ShareDriveResourcesActivity.this.shareRole);
                    if (!ShareDriveResourcesActivity.this.shareType.equalsIgnoreCase("anyone")) {
                        userPermission.setEmailAddress(ShareDriveResourcesActivity.this.shareToUser);
                    }
                    userPermission.setAllowFileDiscovery(true);

                    this.mService.permissions().create(dr.getResourceId(), userPermission)
                            .setFields("id")
                            .queue(batch, new JsonBatchCallback<Permission>() {
                                @Override
                                public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
                                    fileInfo.add("Failure : " + dr.getName());
                                }

                                @Override
                                public void onSuccess(Permission permission, HttpHeaders responseHeaders) {
                                    fileInfo.add("Success : " + dr.getName());
                                }
                            });

                    batch.execute();
                }
                return fileInfo;

            } catch (Exception e) {
                this.mLastError = e;
                this.cancel(true);
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            ShareDriveResourcesActivity.this.mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            ShareDriveResourcesActivity.this.mProgress.hide();
            ShareDriveResourcesActivity.this.finish();
            Intent areaDetailsIntent = new Intent(ShareDriveResourcesActivity.this.getApplicationContext(), AreaDetailsActivity.class);
            areaDetailsIntent.putExtra("load_type", "Return");
            areaDetailsIntent.putExtra("load_result", "Area edited successfully.");
            ShareDriveResourcesActivity.this.startActivity(areaDetailsIntent);
        }

        @Override
        protected void onCancelled() {
            ShareDriveResourcesActivity.this.mProgress.hide();
            if (this.mLastError != null) {
                if (this.mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    ShareDriveResourcesActivity.this.showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) this.mLastError)
                                    .getConnectionStatusCode());
                } else if (this.mLastError instanceof UserRecoverableAuthIOException) {
                    ShareDriveResourcesActivity.this.startActivityForResult(
                            ((UserRecoverableAuthIOException) this.mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                }
            }
            ShareDriveResourcesActivity.this.finish();
        }
    }
}