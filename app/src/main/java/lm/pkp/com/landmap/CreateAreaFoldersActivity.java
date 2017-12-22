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
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserElement;

public class CreateAreaFoldersActivity extends BaseDriveActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_synchronize_folders);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        new FileProcessingTask().execute();
    }

    @Override
    protected void handleConnectionIssues() {
        Intent areaDetailsIntent = new Intent(getApplicationContext(), AreaDetailsActivity.class);
        areaDetailsIntent.putExtra("action", "Drive Error");
        areaDetailsIntent.putExtra("outcome_type", "error");
        areaDetailsIntent.putExtra("outcome", "Place folder creation failed.");
        startActivity(areaDetailsIntent);
    }

    private class FileProcessingTask extends AsyncTask {

        private final Stack<DriveResource> createStack = new Stack<>();

        @Override
        protected Object doInBackground(Object[] params) {
            this.fetchStatusAndAct();
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
            AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();

            DriveResource imagesFolder = commonResourceMap.get(FileStorageConstants.IMAGE_ROOT_FOLDER_NAME);
            DriveResource folderResource = createFolderResource(areaElement.getUniqueId(), imagesFolder,
                    FileStorageConstants.IMAGES_CONTENT_TYPE);
            this.createStack.push(folderResource);

            DriveResource videosFolder = commonResourceMap.get(FileStorageConstants.VIDEO_ROOT_FOLDER_NAME);
            folderResource = createFolderResource(areaElement.getUniqueId(), videosFolder,
                    FileStorageConstants.VIDEOS_CONTENT_TYPE);
            this.createStack.push(folderResource);

            DriveResource docsFolder = commonResourceMap.get(FileStorageConstants.DOCUMENT_ROOT_FOLDER_NAME);
            folderResource = createFolderResource(areaElement.getUniqueId(), docsFolder,
                    FileStorageConstants.DOCUMENTS_CONTENT_TYPE);
            this.createStack.push(folderResource);

            // Start processing.
            this.processCreateStack();
        }

        public void processCreateStack() {
            if (this.createStack.isEmpty()) {
                getGoogleApiClient().disconnect();
                Intent i = new Intent(getApplicationContext(), AreaDetailsActivity.class);
                startActivity(i);
            } else {
                DriveResource resource = this.createStack.pop();
                Drive.DriveApi.fetchDriveId(getGoogleApiClient(), resource.getContainerId())
                        .setResultCallback(new DriveIdResultCallback(resource));

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
                        = new Builder().setTitle(this.resource.getName())
                        .setMimeType(this.resource.getMimeType()).build();
                parentFolder.createFolder(getGoogleApiClient(), changeSet)
                        .setResultCallback(new CreateFolderCallback(this.resource));
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
                        .setTitle(this.resource.getName())
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
                    this.resource.setResourceId(resourceId);

                    DriveResourceInsertCallback callback = new DriveResourceInsertCallback(this.resource);
                    DriveDBHelper ddh = new DriveDBHelper(getApplicationContext(), callback);
                    ddh.insertResourceLocally(this.resource);
                    ddh.insertResourceToServer(this.resource);
                }
            }

            private class DriveResourceInsertCallback implements AsyncTaskCallback {

                private final DriveResource resource;

                public DriveResourceInsertCallback(DriveResource resource) {
                    this.resource = resource;
                }

                @Override
                public void taskCompleted(Object result) {
                    FileProcessingTask.this.processCreateStack();
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

        return resource;
    }

}
