package lm.pkp.com.landmap;

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
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Builder;
import com.google.api.services.drive.DriveScopes;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks;

public class RemoveDriveResourcesActivity extends Activity implements PermissionCallbacks {

    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {DriveScopes.DRIVE_METADATA_READONLY,
            DriveScopes.DRIVE, DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA};

    private String resourceIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        this.setContentView(layout.activity_remove_resources);

        this.mProgress = new ProgressDialog(this);
        this.mProgress.setMessage("Removing area files ...");
        this.mCredential = GoogleAccountCredential.usingOAuth2(this.getApplicationContext(),
                Arrays.asList(RemoveDriveResourcesActivity.SCOPES)).setBackOff(new ExponentialBackOff());

        Bundle extras = this.getIntent().getExtras();
        if (extras == null) {
            this.removeResources(this.resourceIds);
        } else {
            String resourceIds = extras.getString("resource_ids");
            if (resourceIds != null) {
                this.resourceIds = resourceIds;
            }
            this.removeResources(resourceIds);
        }
    }

    private void removeResources(String resourceIds) {
        if (!this.isGooglePlayServicesAvailable()) {
            this.acquireGooglePlayServices();
        } else if (this.mCredential.getSelectedAccountName() == null) {
            this.chooseAccount();
        } else {
            if (resourceIds == null) {
                new RemoveRequestTask(this.mCredential).execute();
            } else {
                new RemoveRequestTask(this.mCredential, resourceIds).execute();
            }
        }
    }

    @AfterPermissionGranted(RemoveDriveResourcesActivity.REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, permission.GET_ACCOUNTS)) {
            String accountName = this.getPreferences(Context.MODE_PRIVATE)
                    .getString(RemoveDriveResourcesActivity.PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                this.mCredential.setSelectedAccountName(accountName);
                this.removeResources(this.resourceIds);
            } else {
                // Start a dialog from which the user can choose an account
                this.startActivityForResult(this.mCredential.newChooseAccountIntent(), RemoveDriveResourcesActivity.REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account.",
                    RemoveDriveResourcesActivity.REQUEST_PERMISSION_GET_ACCOUNTS,
                    permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RemoveDriveResourcesActivity.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    this.removeResources(null);
                } else {
                    // TODO Share the error with the user
                }
                break;
            case RemoveDriveResourcesActivity.REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = this.getPreferences(Context.MODE_PRIVATE);
                        Editor editor = settings.edit();
                        editor.putString(RemoveDriveResourcesActivity.PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        this.mCredential.setSelectedAccountName(accountName);
                        this.removeResources(this.resourceIds);
                    }
                }
                break;
            case RemoveDriveResourcesActivity.REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    this.removeResources(this.resourceIds);
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
        Dialog dialog = apiAvailability.getErrorDialog(this, connectionStatusCode, RemoveDriveResourcesActivity.REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private class RemoveRequestTask extends AsyncTask<Void, Void, Boolean> {
        private Drive mService;
        private Exception mLastError;
        private String resourceIds;

        private RemoveRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            this.mService = new Builder(transport, jsonFactory, credential)
                    .setApplicationName("LMS").build();
        }

        private RemoveRequestTask(GoogleAccountCredential credential, String resourceIds) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            this.mService = new Builder(transport, jsonFactory, credential)
                    .setApplicationName("LMS").build();
            this.resourceIds = resourceIds;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                AreaContext ac = AreaContext.INSTANCE;
                AreaElement ae = ac.getAreaElement();
                DriveDBHelper ddh = new DriveDBHelper(RemoveDriveResourcesActivity.this.getApplicationContext());
                if (this.resourceIds != null) {
                    String[] resourceArr = this.resourceIds.split(",");
                    for (int i = 0; i < resourceArr.length; i++) {
                        String resourceID = resourceArr[i];
                        DriveResource fetchedResource = ddh.getDriveResourceByResourceId(resourceID);
                        ddh.deleteResourceByResourceId(resourceID);
                        ae.getMediaResources().remove(fetchedResource);

                        String contentType = fetchedResource.getContentType();
                        String resourceRootPath = null;
                        String thumbnailRootPath = null;
                        if (contentType.equalsIgnoreCase("Image")) {
                            resourceRootPath = ac.getAreaLocalImageRoot(ae.getUniqueId()).getAbsolutePath();
                            thumbnailRootPath = ac.getAreaLocalPictureThumbnailRoot(ae.getUniqueId()).getAbsolutePath();
                        } else if (contentType.equalsIgnoreCase("Video")) {
                            resourceRootPath = ac.getAreaLocalVideoRoot(ae.getUniqueId()).getAbsolutePath();
                            thumbnailRootPath = ac.getAreaLocalVideoThumbnailRoot(ae.getUniqueId()).getAbsolutePath();
                        } else {
                            resourceRootPath = ac.getAreaLocalDocumentRoot(ae.getUniqueId()).getAbsolutePath();
                            thumbnailRootPath = ac.getAreaLocalDocumentThumbnailRoot(ae.getUniqueId()).getAbsolutePath();
                        }

                        File localFile = new File(resourceRootPath + File.separatorChar + fetchedResource.getName());
                        if (localFile.exists()) {
                            localFile.delete();
                        }

                        File thumbFile = new File(thumbnailRootPath + File.separatorChar + fetchedResource.getName());
                        if (thumbFile.exists()) {
                            thumbFile.delete();
                        }
                        try {
                            this.mService.files().delete(resourceID).execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    List<DriveResource> resources = ae.getMediaResources();
                    for (int j = 0; j < resources.size(); j++) {
                        DriveResource resource = resources.get(j);
                        if (resource.getType().equalsIgnoreCase("File")) {
                            File storeRoot = ac.getLocalStoreLocationForDriveResource(resource);
                            if (storeRoot != null) {
                                if (storeRoot.exists()) {
                                    FileUtils.deleteDirectory(storeRoot);
                                }
                            }
                            continue;
                        }
                        if (!resource.getContainerId().trim().equalsIgnoreCase("")) {
                            // Delete only the area specific folders and not the common folders.
                            try {
                                this.mService.files().delete(resource.getResourceId()).execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            } catch (Exception e) {
                this.mLastError = e;
                this.cancel(true);
                return null;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Intent displayIntent = null;
            if (this.resourceIds != null) {
                displayIntent = new Intent(RemoveDriveResourcesActivity.this.getApplicationContext(), AreaResourceDisplayActivity.class);
                displayIntent.putExtras(RemoveDriveResourcesActivity.this.getIntent().getExtras());
            } else {
                displayIntent = new Intent(RemoveDriveResourcesActivity.this.getApplicationContext(), AreaDashboardActivity.class);
            }
            RemoveDriveResourcesActivity.this.startActivity(displayIntent);
            RemoveDriveResourcesActivity.this.finish();
        }

        @Override
        protected void onCancelled() {
            RemoveDriveResourcesActivity.this.mProgress.hide();
            if (this.mLastError != null) {
                if (this.mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    RemoveDriveResourcesActivity.this.showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) this.mLastError)
                                    .getConnectionStatusCode());
                } else if (this.mLastError instanceof UserRecoverableAuthIOException) {
                    RemoveDriveResourcesActivity.this.startActivityForResult(
                            ((UserRecoverableAuthIOException) this.mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                }
            }
            RemoveDriveResourcesActivity.this.finish();
        }
    }
}