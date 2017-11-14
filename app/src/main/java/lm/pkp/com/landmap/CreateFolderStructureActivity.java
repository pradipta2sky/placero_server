/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Stack;
import java.util.UUID;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserElement;

public class CreateFolderStructureActivity extends BaseDriveActivity {

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

        private final String IMAGE_FOLDER_NAME = "LMS_IMAGES";
        private final String VIDEO_FOLDER_NAME = "LMS_VIDEOS";
        private final String DOCUMENT_FOLDER_NAME = "LMS_DOCS";

        private LinkedHashMap<String, DriveResource> resourceMap = new LinkedHashMap<>();
        private Stack<DriveResource> processStack = new Stack<>();

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
            AreaElement areaElement = AreaContext.getInstance().getAreaElement();
            ArrayList<DriveResource> areaFolders = ddh.getDriveResourcesByAreaId(areaElement.getUniqueId(), "folder");
            // First check if there are DB entries.
            for (DriveResource res : areaFolders) {
                String folderName = res.getName();
                if (folderName.equals(IMAGE_FOLDER_NAME)) {
                    findFolderAndAct(res);
                    resourceMap.put(IMAGE_FOLDER_NAME, res);
                } else if (folderName.equals(VIDEO_FOLDER_NAME)) {
                    findFolderAndAct(res);
                    resourceMap.put(VIDEO_FOLDER_NAME, res);
                } else if (folderName.equals(DOCUMENT_FOLDER_NAME)) {
                    findFolderAndAct(res);
                    resourceMap.put(DOCUMENT_FOLDER_NAME, res);
                }
            }
            // Now check if any DB entry is missing.
            if (resourceMap.get(IMAGE_FOLDER_NAME) == null) {
                // Create Image folder
                generateMetaAndCreate(IMAGE_FOLDER_NAME);
            }
            if (resourceMap.get(VIDEO_FOLDER_NAME) == null) {
                // Create Video folder
                generateMetaAndCreate(VIDEO_FOLDER_NAME);
            }
            if (resourceMap.get(DOCUMENT_FOLDER_NAME) == null) {
                // Create Docs folder
                generateMetaAndCreate(DOCUMENT_FOLDER_NAME);
            }

            // Start processing.
            process();
        }

        public void process() {
            if (processStack.isEmpty()) {
                Intent i = new Intent(CreateFolderStructureActivity.this, AreaAddResourcesActivity.class);
                startActivity(i);
            } else {
                DriveResource res = processStack.pop();
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(res.getName())
                        .setMimeType(res.getMimeType())
                        .build();
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

        private void findFolderAndAct(DriveResource res) {
            DriveApi.DriveIdResult idResult = Drive.DriveApi.fetchDriveId(getGoogleApiClient(),
                    res.getDriveResourceId()).await();
            if (!idResult.getStatus().isSuccess()) {
                createNewFolder(res);
            }
        }

        private void generateMetaAndCreate(String folderName) {
            AreaElement ae = AreaContext.getInstance().getAreaElement();
            UserElement ue = UserContext.getInstance().getUserElement();

            DriveResource dr = new DriveResource();
            dr.setUniqueId(UUID.randomUUID().toString());
            dr.setAreaId(ae.getUniqueId());
            dr.setType("folder");
            dr.setUserId(ue.getEmail());
            dr.setName(folderName);
            dr.setContainerDriveId("");
            dr.setMimeType("application/vnd.google-apps.folder");
            dr.setContentType("any");
            dr.setSize("0");

            createNewFolder(dr);
        }

        private void createNewFolder(DriveResource res) {
            processStack.push(res);
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
                if(resourceId != null){
                    eventFolder.removeChangeListener(getGoogleApiClient(), this);
                    DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());

                    resource.setDriveResourceId(resourceId);
                    resource.setDriveId(driveId.toString());

                    ddh.insertResourceLocally(resource);
                    ddh.insertResourceToServer(resource);

                    process();
                }
            }
        }

    }

}
