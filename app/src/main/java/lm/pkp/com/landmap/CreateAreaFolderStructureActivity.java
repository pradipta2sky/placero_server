package lm.pkp.com.landmap;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;

import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.FileStorageConstants;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserElement;

public class CreateAreaFolderStructureActivity extends BaseDriveActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronize_folders);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        new FileProcessingTask().execute();
    }

    private class FileProcessingTask extends AsyncTask {

        private Stack<DriveResource> createStack = new Stack<>();

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
            Map<String, DriveResource> commonResourceMap = ddh.getCommonResources();
            final AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();

            final DriveResource imagesFolder = commonResourceMap.get(FileStorageConstants.IMAGE_ROOT_FOLDER_NAME);
            DriveResource folderResource = createFolderResource(areaElement.getUniqueId(), imagesFolder);
            createStack.push(folderResource);

            final DriveResource videosFolder = commonResourceMap.get(FileStorageConstants.VIDEO_ROOT_FOLDER_NAME);
            folderResource = createFolderResource(areaElement.getUniqueId(), videosFolder);
            createStack.push(folderResource);

            final DriveResource docsFolder = commonResourceMap.get(FileStorageConstants.DOCUMENT_ROOT_FOLDER_NAME);
            folderResource = createFolderResource(areaElement.getUniqueId(), docsFolder);
            createStack.push(folderResource);

            // Start processing.
            processCreateStack();
        }

        public void processCreateStack() {
            if (createStack.isEmpty()) {
                getGoogleApiClient().disconnect();
                Intent i = new Intent(CreateAreaFolderStructureActivity.this, AreaDetailsActivity.class);
                startActivity(i);
            } else {
                DriveResource resource = createStack.pop();
                Drive.DriveApi.fetchDriveId(getGoogleApiClient(), resource.getContainerId())
                        .setResultCallback(new DriveIdResultCallback(resource));

            }
        }

        private class DriveIdResultCallback implements ResultCallback<DriveApi.DriveIdResult> {

            private DriveResource resource;

            public DriveIdResultCallback(DriveResource resource) {
                this.resource = resource;
            }

            @Override
            public void onResult(DriveApi.DriveIdResult driveIdResult) {
                final DriveFolder parentFolder = driveIdResult.getDriveId().asDriveFolder();
                MetadataChangeSet changeSet
                        = new MetadataChangeSet.Builder().setTitle(resource.getName())
                        .setMimeType(resource.getMimeType()).build();
                parentFolder.createFolder(getGoogleApiClient(), changeSet)
                        .setResultCallback(new CreateFolderCallback(resource));
            }
        }

        private class CreateFolderCallback implements ResultCallback<DriveFolderResult> {

            private DriveResource resource = null;

            public CreateFolderCallback(DriveResource resource) {
                this.resource = resource;
            }

            @Override
            public void onResult(DriveFolderResult folderResult) {
                DriveFolder driveFolder = folderResult.getDriveFolder();
                driveFolder.addChangeListener(getGoogleApiClient(), new FolderMetaChangeListener(resource));
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(resource.getName())
                        .build();
                driveFolder.updateMetadata(getGoogleApiClient(), changeSet);
            }
        }

        private class FolderMetaChangeListener implements ChangeListener {

            private DriveResource resource = null;

            public FolderMetaChangeListener(DriveResource res) {
                resource = res;
            }

            @Override
            public void onChange(ChangeEvent changeEvent) {
                DriveFolder eventFolder = changeEvent.getDriveId().asDriveFolder();
                DriveId driveId = eventFolder.getDriveId();
                String resourceId = driveId.getResourceId();
                if ((resourceId != null) && (resource.getResourceId() == null)) {
                    eventFolder.removeChangeListener(getGoogleApiClient(), this);
                    resource.setResourceId(resourceId);

                    AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
                    AreaContext.INSTANCE.setAreaElement(areaElement, getApplicationContext());

                    DriveDBHelper ddh
                            = new DriveDBHelper(getApplicationContext(), new DriveResourceInsertCallback(resource));
                    ddh.insertResourceLocally(resource);
                    ddh.insertResourceToServer(resource);
                }
            }

            private class DriveResourceInsertCallback implements AsyncTaskCallback{

                private DriveResource resource;

                public DriveResourceInsertCallback(DriveResource resource){
                    this.resource = resource;
                }

                @Override
                public void taskCompleted(Object result) {
                    processCreateStack();
                }
            }
        }

    }

    private DriveResource createFolderResource(String folderName, DriveResource parent) {
        DriveResource resource = new DriveResource();

        final AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        final UserElement userElement = UserContext.getInstance().getUserElement();

        resource.setUniqueId(UUID.randomUUID().toString());
        resource.setName(folderName);
        resource.setType("folder");
        resource.setUserId(userElement.getEmail());
        resource.setSize("0");
        resource.setAreaId(areaElement.getUniqueId());
        resource.setContainerId(parent.getResourceId());
        resource.setContentType("folder");
        resource.setMimeType("application/vnd.google-apps.folder");
        resource.setResourceId(null);

        return resource;
    }

}
