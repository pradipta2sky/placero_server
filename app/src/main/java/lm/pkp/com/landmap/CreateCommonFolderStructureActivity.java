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
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import lm.pkp.com.landmap.area.FileStorageConstants;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.user.UserContext;

public class CreateCommonFolderStructureActivity extends BaseDriveActivity {

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
            final Map<String, DriveResource> commonResourceMap = ddh.getCommonResources();
            if(commonResourceMap.size() == 0){
                // If no create common folders in database.
                DriveResource commonImageFolder = createCommonFolder(FileStorageConstants.IMAGE_ROOT_FOLDER_NAME);
                // If no create folder in drive.
                createStack.push(commonImageFolder);

                DriveResource commonVideosFolder = createCommonFolder(FileStorageConstants.VIDEO_ROOT_FOLDER_NAME);
                // If no create folder in drive.
                createStack.push(commonVideosFolder);

                DriveResource commonDocsFolder = createCommonFolder(FileStorageConstants.DOCUMENT_ROOT_FOLDER_NAME);
                // If no create folder in drive.
                createStack.push(commonDocsFolder);
            }else {
                // Check for title and resourceID match between database information and drive folder.
                // Prepare the query for drive folder list.
                Map<String, Boolean> commonResourceStatusMap = new HashMap<>();
                Map<String, Metadata> commonMetadataMap = new HashMap<>();
                Map<String, Metadata> commonDefunctMetadataMap = new HashMap<>();

                final Collection<DriveResource> commonResources = commonResourceMap.values();
                final Iterator<DriveResource> commonResourcesIter = commonResources.iterator();
                while (commonResourcesIter.hasNext()){
                    final DriveResource commonResource = commonResourcesIter.next();
                    commonResourceStatusMap.put(commonResource.getName(), false);
                }

                final DriveApi.MetadataBufferResult bufferResult = Drive.DriveApi.getRootFolder(getGoogleApiClient())
                        .listChildren(getGoogleApiClient()).await();
                // Match the obtained results with that stored in database.
                final MetadataBuffer metadataBuffer = bufferResult.getMetadataBuffer();
                final Iterator<Metadata> bufferIter = metadataBuffer.iterator();
                while (bufferIter.hasNext()){
                    final Metadata metadata = bufferIter.next();
                    final DriveResource resource = commonResourceMap.get(metadata.getTitle());
                    // Got a common resource with same name.
                    if(resource == null){
                        continue;
                    }
                    if(resource.getResourceId().equals(metadata.getDriveId().getResourceId())){
                        // Both match
                        commonResourceStatusMap.put(resource.getName(), true);
                        commonMetadataMap.put(resource.getName(), metadata);
                    }else {
                        // Title matches but resourceIds do not match
                        commonDefunctMetadataMap.put(resource.getName(), metadata);
                    }
                }
                // Check the status and push it to the stack
                final Iterator<String> commonResourceIter = commonResourceStatusMap.keySet().iterator();
                while(commonResourceIter.hasNext()){
                    final String resourceKey = commonResourceIter.next();
                    final DriveResource resource = commonResourceMap.get(resourceKey);
                    final Boolean resourceStatus = commonResourceStatusMap.get(resourceKey);
                    if(!resourceStatus){
                        final Metadata metadata = commonDefunctMetadataMap.get(resourceKey);
                        if(metadata != null){
                            // A different folder with same name exists.
                            // Use it for this application.
                            resource.setResourceId(metadata.getDriveId().getResourceId());
                            ddh.deleteResourceByResourceId(resource.getResourceId());
                            ddh.insertResourceLocally(resource);
                            ddh.updateResourceOnServer(resource);
                        }else {
                            // Folder does not exist. Create one.
                            createStack.push(resource);
                        }
                    }else {
                        // Resource is available. Do nothing.
                    }
                }
            }

            // Start processing.
            processCreateStack();
        }

        public void processCreateStack() {
            if (createStack.isEmpty()) {
                getGoogleApiClient().disconnect();
                Intent i = new Intent(CreateCommonFolderStructureActivity.this, AreaDashboardActivity.class);
                startActivity(i);
                finish();
            } else {
                DriveResource res = createStack.pop();
                MetadataChangeSet changeSet
                        = new MetadataChangeSet.Builder().setTitle(res.getName()).setMimeType(res.getMimeType()).build();
                Drive.DriveApi.getRootFolder(getGoogleApiClient())
                        .createFolder(getGoogleApiClient(), changeSet).setResultCallback(new CreateFolderCallback(res));
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
                        .setStarred(true)
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
                    DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());

                    resource.setResourceId(resourceId);
                    ddh.insertResourceLocally(resource);
                    ddh.insertResourceToServer(resource);

                    processCreateStack();
                }
            }
        }

    }

    private DriveResource createCommonFolder(String commonFolderName) {
        DriveResource resource = new DriveResource();
        resource.setUniqueId(UUID.randomUUID().toString());
        resource.setName(commonFolderName);
        resource.setType("folder");
        resource.setUserId(UserContext.getInstance().getUserElement().getEmail());
        resource.setSize("0");
        resource.setAreaId("");
        resource.setResourceId(null);
        resource.setContainerId("");
        resource.setContentType("folder");
        resource.setMimeType("application/vnd.google-apps.folder");
        return resource;
    }

}
