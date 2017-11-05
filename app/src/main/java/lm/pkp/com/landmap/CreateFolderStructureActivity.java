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
import android.os.Bundle;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.DriveResource.MetadataResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;

public class CreateFolderStructureActivity extends BaseDriveActivity {

    private final String IMAGE_FOLDER_NAME = "LMS_IMAGES";
    private final String VIDEO_FOLDER_NAME = "LMS_VIDEOS";
    private final String DOCUMENT_FOLDER_NAME = "LMS_DOCS";

    private LinkedHashMap<String, String> resStatusMap = new LinkedHashMap<>();
    private LinkedHashMap<String, String> driveStatusMap = new LinkedHashMap<>();
    private LinkedHashMap<String, DriveResource> resourceMap = new LinkedHashMap<>();

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);

        resStatusMap.put(IMAGE_FOLDER_NAME, "false");
        resStatusMap.put(VIDEO_FOLDER_NAME, "false");
        resStatusMap.put(DOCUMENT_FOLDER_NAME, "false");

        driveStatusMap.put(IMAGE_FOLDER_NAME, "false");
        driveStatusMap.put(VIDEO_FOLDER_NAME, "false");
        driveStatusMap.put(DOCUMENT_FOLDER_NAME, "false");

        fetchStatusAndAct();
    }

    private void fetchStatusAndAct() {
        DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
        ArrayList<DriveResource> allDriveFolders = ddh.getFolderResourcesByUid(ue.getEmail());

        for (DriveResource res : allDriveFolders) {
            String folderName = res.getName();
            if(folderName.equals(IMAGE_FOLDER_NAME)){
                resStatusMap.put(IMAGE_FOLDER_NAME, "true");
                resourceMap.put(IMAGE_FOLDER_NAME, res);
                DriveId folderId = DriveId.decodeFromString(res.getDriveId());
                DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(), folderId);
                if(folder != null){
                    driveStatusMap.put(IMAGE_FOLDER_NAME, "true");
                }
            }else if(folderName.equals(VIDEO_FOLDER_NAME)){
                resStatusMap.put(VIDEO_FOLDER_NAME, "true");
                resourceMap.put(VIDEO_FOLDER_NAME, res);
                DriveId folderId = DriveId.decodeFromString(res.getDriveId());
                DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(), folderId);
                if(folder != null){
                    driveStatusMap.put(VIDEO_FOLDER_NAME, "true");
                }
            }else if(folderName.equals(DOCUMENT_FOLDER_NAME)){
                resStatusMap.put(DOCUMENT_FOLDER_NAME, "true");
                resourceMap.put(DOCUMENT_FOLDER_NAME, res);
                DriveId folderId = DriveId.decodeFromString(res.getDriveId());
                DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(), folderId);
                if(folder != null){
                    driveStatusMap.put(DOCUMENT_FOLDER_NAME, "true");
                }
            }
        }
        actOnStatusObtained();
    }

    private void actOnStatusObtained() {
        DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
        for (String key : resStatusMap.keySet()) {
            String resStatusStr = resStatusMap.get(key);
            Boolean resStatus = new Boolean(resStatusStr);
            if(!resStatus){
                createFolder(key);
            }else {
                String driveStatusStr = driveStatusMap.get(key);
                Boolean driveStatus = new Boolean(driveStatusStr);
                if(!driveStatus){
                    // The drive resource got deleted somehow.
                    ddh.deleteResource(resourceMap.get(key));
                    createFolder(key);
                }
            }
        }
        if(creationComplete()){
            Intent i = new Intent(CreateFolderStructureActivity.this, AreaAddResourcesActivity.class);
            startActivity(i);
        }
    }

    private void createFolder(String folderName) {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(folderName).build();
        Drive.DriveApi.getRootFolder(getGoogleApiClient()).createFolder(
                getGoogleApiClient(), changeSet).setResultCallback(new GenericResultCallback(folderName));
    }

    private class GenericResultCallback implements ResultCallback<DriveFolderResult>{

        private String folderName = null;

        public GenericResultCallback(String folderName){
            this.folderName = folderName;
        }

        @Override
        public void onResult(DriveFolderResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Problem while trying to create a folder");
                return;
            }
            DriveFolder folder = result.getDriveFolder();
            folder.getMetadata(getGoogleApiClient()).setResultCallback(new GenericMetaDataCallback(folderName));
        }
    }

    private class GenericMetaDataCallback implements ResultCallback<MetadataResult>{

        private String folderName = null;

        public GenericMetaDataCallback(String folderName){
            this.folderName = folderName;
        }

        @Override
        public void onResult(MetadataResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Problem while trying to fetch metadata");
                return;
            }
            Metadata metadata = result.getMetadata();
            // Insert in database here.
            DriveResource dr = new DriveResource();
            dr.setUniqueId(UUID.randomUUID().toString());
            dr.setAreaId(ae.getUniqueId());
            dr.setType("folder");
            dr.setUserId(ue.getEmail());
            dr.setDriveId(metadata.getDriveId().toString());
            dr.setSize(metadata.getFileSize() + "");
            dr.setName(metadata.getTitle());
            dr.setContainerDriveId("");
            dr.setMimeType("application/vnd.google-apps.folder");
            dr.setDriveResourceId(metadata.getDriveId().getResourceId());
            dr.setContentType("any");

            AreaContext.getInstance().getAreaElement().getDriveResources().add(dr);

            DriveDBHelper ddh = new DriveDBHelper(getApplicationContext(),new ServerUpdateCallback(folderName));
            ddh.insertResourceLocally(dr);
            ddh.insertResourceToServer(dr);
        }
    }

    private class ServerUpdateCallback implements AsyncTaskCallback{

        private String key = null;

        public ServerUpdateCallback(String key){
            this.key = key;
        }

        @Override
        public void taskCompleted(Object result) {
            resStatusMap.put(key, "true");
            driveStatusMap.put(key, "true");
            if(creationComplete()){
                Intent i = new Intent(CreateFolderStructureActivity.this, AreaAddResourcesActivity.class);
                startActivity(i);
            }
        }
    }

    private boolean creationComplete() {
        boolean isComplete = true;
        for (String key : resStatusMap.keySet()) {
            String resStatusStr = resStatusMap.get(key);
            Boolean resStatus = new Boolean(resStatusStr);

            String driveStatusStr = driveStatusMap.get(key);
            Boolean driveStatus = new Boolean(driveStatusStr);

            if(!resStatus && !driveStatus){
                isComplete = false;
            }
        }
        return isComplete;
    }
}
