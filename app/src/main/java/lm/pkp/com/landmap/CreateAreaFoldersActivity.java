package lm.pkp.com.landmap;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.MetadataChangeSet.Builder;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;

import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.area.FileStorageConstants;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.custom.GlobalContext;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserElement;

public class CreateAreaFoldersActivity extends BaseDriveActivity {

    private boolean online = true;
    private boolean execBackground = false;
    private String areaId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronize_folders);

        Bundle extras = getIntent().getExtras();
        areaId = extras.getString("area_id");
        String exec = extras.getString("exec_background");
        if(exec != null){
            execBackground = new Boolean(exec);
            moveTaskToBack(execBackground);
        }
        online = new Boolean(GlobalContext.INSTANCE.get(GlobalContext.INTERNET_AVAILABLE));
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        new FileProcessingTask().execute();
    }

    @Override
    protected void handleConnectionIssues() {
        Intent areaDetailsIntent = new Intent(getApplicationContext(), AreaDetailsActivity.class);
        areaDetailsIntent.putExtra("action", "Drive Connection Error");
        areaDetailsIntent.putExtra("outcome_type", "error");
        areaDetailsIntent.putExtra("outcome", "Folder creation failed.");
        startActivity(areaDetailsIntent);
    }

    private class FileProcessingTask extends AsyncTask {

        private final Stack<DriveResource> createStack = new Stack<>();

        @Override
        protected Object doInBackground(Object[] params) {
            fetchStatusAndAct();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }


        private void fetchStatusAndAct() {
            DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
            // Check if there are common folders defined in database. //content_type = folder
            Map<String, DriveResource> commonResourceMap = ddh.getCommonResourcesByName();

            DriveResource imagesFolder = commonResourceMap.get(FileStorageConstants.IMAGE_ROOT_FOLDER_NAME);
            DriveResource folderResource = createFolderResource(areaId, imagesFolder,
                    FileStorageConstants.IMAGES_CONTENT_TYPE);
            createStack.push(folderResource);

            DriveResource videosFolder = commonResourceMap.get(FileStorageConstants.VIDEO_ROOT_FOLDER_NAME);
            folderResource = createFolderResource(areaId, videosFolder,
                    FileStorageConstants.VIDEOS_CONTENT_TYPE);
            createStack.push(folderResource);

            DriveResource docsFolder = commonResourceMap.get(FileStorageConstants.DOCUMENT_ROOT_FOLDER_NAME);
            folderResource = createFolderResource(areaId, docsFolder,
                    FileStorageConstants.DOCUMENTS_CONTENT_TYPE);
            createStack.push(folderResource);

            // Start processing.
            processCreateStack();
        }

        public void processCreateStack() {
            DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
            if (createStack.isEmpty()) {
                getGoogleApiClient().disconnect();
                Intent i = new Intent(getApplicationContext(), AreaDetailsActivity.class);
                startActivity(i);
            } else {
                DriveResource resource = createStack.pop();
                if(online){
                    Drive.DriveApi.fetchDriveId(getGoogleApiClient(), resource.getContainerId())
                            .setResultCallback(new DriveIdResultCallback(resource));
                }else {
                    ddh.insertResourceLocally(resource);
                }
            }
        }

        private class DriveIdResultCallback implements ResultCallback<DriveIdResult> {

            private final DriveResource resource;

            public DriveIdResultCallback(DriveResource resource) {
                this.resource = resource;
            }

            @Override
            public void onResult(DriveIdResult driveIdResult) {
                DriveFolder parentFolder = driveIdResult.getDriveId().asDriveFolder();
                MetadataChangeSet changeSet
                        = new Builder().setTitle(resource.getName())
                        .setMimeType(resource.getMimeType()).build();
                parentFolder.createFolder(getGoogleApiClient(), changeSet)
                        .setResultCallback(new CreateFolderCallback(resource));
            }
        }

        private class CreateFolderCallback implements ResultCallback<DriveFolder.DriveFolderResult> {

            private DriveResource resource;

            public CreateFolderCallback(DriveResource resource) {
                this.resource = resource;
            }

            @Override
            public void onResult(DriveFolder.DriveFolderResult folderResult) {
                DriveFolder driveFolder = folderResult.getDriveFolder();
                driveFolder.addChangeListener(getGoogleApiClient(), new FolderMetaChangeListener(this.resource));
                MetadataChangeSet changeSet = new Builder()
                        .setTitle(resource.getName())
                        .build();
                driveFolder.updateMetadata(getGoogleApiClient(), changeSet);
            }
        }

        private class FolderMetaChangeListener implements ChangeListener {

            private DriveResource resource;

            public FolderMetaChangeListener(DriveResource res) {
                this.resource = res;
            }

            @Override
            public void onChange(ChangeEvent changeEvent) {
                DriveFolder eventFolder = changeEvent.getDriveId().asDriveFolder();
                DriveId driveId = eventFolder.getDriveId();
                String resourceId = driveId.getResourceId();
                if (resourceId != null && resource.getResourceId() == null) {
                    eventFolder.removeChangeListener(getGoogleApiClient(), this);
                    resource.setResourceId(resourceId);

                    DriveResourceInsertCallback callback = new DriveResourceInsertCallback(this.resource);
                    DriveDBHelper ddh = new DriveDBHelper(getApplicationContext(), callback);
                    // This is to handle offline insertion
                    ddh.deleteResourceLocally(resource);
                    ddh.insertResourceLocally(resource);
                    ddh.insertResourceToServer(resource);
                }
            }

            private class DriveResourceInsertCallback implements AsyncTaskCallback {

                private final DriveResource resource;

                public DriveResourceInsertCallback(DriveResource resource) {
                    this.resource = resource;
                }

                @Override
                public void taskCompleted(Object result) {
                    processCreateStack();
                }
            }
        }

    }

    private DriveResource createFolderResource(String folderName, DriveResource parent, String contentType) {
        DriveResource resource = new DriveResource();

        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        UserElement userElement = UserContext.getInstance().getUserElement();

        resource.setUniqueId(UUID.randomUUID().toString());
        resource.setName(folderName);
        resource.setType("folder");
        resource.setUserId(userElement.getEmail());
        resource.setSize("0");
        resource.setAreaId(areaElement.getUniqueId());
        resource.setContainerId(parent.getResourceId());
        resource.setContentType(contentType);
        resource.setMimeType("application/vnd.google-apps.folder");
        resource.setResourceId(null);
        if(!online){
            resource.setDirty(1);
            resource.setDirtyAction("insert");
        }
        return resource;
    }

}
