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

package lm.pkp.com.landmap.google.drive;

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
import java.util.UUID;

import lm.pkp.com.landmap.AreaAddResourcesActivity;
import lm.pkp.com.landmap.drive.DriveResource;

/**
 * An activity to illustrate how to create a new folder.
 */
public class CreateFolderStructureActivity extends BaseDriveActivity {

    private final String ROOT_FOLDER_NAME = "LMS_RES_ROOT";
    private final String IMAGE_FOLDER_NAME = "LMS_IMAGES";
    private final String VIDEO_FOLDER_NAME = "LMS_VIDEOS";
    private final String DOCUMENT_FOLDER_NAME = "LMS_DOCS";

    private DriveFolder rootFolder = null;
    private DriveFolder imagesFolder = null;
    private DriveFolder videosFolder = null;
    private DriveFolder documentsFolder = null;

    private DriveResource rootResource = null;
    private DriveResource imagesResource = null;
    private DriveResource videosResource = null;
    private DriveResource documentsResource = null;


    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        ArrayList<DriveResource> allDriveFolders = ddh.getFolderResourcesByUid(ue.getEmail());
        for (DriveResource res : allDriveFolders) {
            String folderName = res.getName();
            if(folderName.equals(ROOT_FOLDER_NAME)){
                rootResource = res;
                DriveId folderId = DriveId.decodeFromString(res.getDriveId());
                rootFolder = Drive.DriveApi.getFolder(getGoogleApiClient(), folderId);
            }else if(folderName.equals(IMAGE_FOLDER_NAME)){
                imagesResource = res;
                DriveId folderId = DriveId.decodeFromString(res.getDriveId());
                imagesFolder = Drive.DriveApi.getFolder(getGoogleApiClient(), folderId);
            }else if(folderName.equals(VIDEO_FOLDER_NAME)){
                videosResource = res;
                DriveId folderId = DriveId.decodeFromString(res.getDriveId());
                videosFolder = Drive.DriveApi.getFolder(getGoogleApiClient(), folderId);
            }else if(folderName.equals(DOCUMENT_FOLDER_NAME)){
                documentsResource = res;
                DriveId folderId = DriveId.decodeFromString(res.getDriveId());
                documentsFolder = Drive.DriveApi.getFolder(getGoogleApiClient(), folderId);
            }
        }
        // TODO Prasanna Server implementation pending.
        if(!isRootFolderCreated()){
            ddh.deleteDriveElementsLocally();
            createRootFolder();
        }

    }

    private void createRootFolder() {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(ROOT_FOLDER_NAME).build();
        Drive.DriveApi.getRootFolder(getGoogleApiClient()).createFolder(
                getGoogleApiClient(), changeSet).setResultCallback(rootCallback);
    }

    final ResultCallback<DriveFolderResult> rootCallback = new ResultCallback<DriveFolderResult>() {
        @Override
        public void onResult(DriveFolderResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create the folder");
                return;
            }
            DriveFolder folder = result.getDriveFolder();
            if (rootFolder == null) {
                rootFolder = folder;
            }

            rootFolder.getMetadata(getGoogleApiClient()).setResultCallback(metadataCallback);

            if(!isImagesFolderCreated()){
                if(imagesResource != null){
                    ddh.deleteResource(imagesResource);
                }
                createImagesFolder();
            }

            if(!isVideosFolderCreated()){
                if(videosResource != null){
                    ddh.deleteResource(videosResource);
                }
                createVideosFolder();
            }

            if(!isDocumentsFolderCreated()){
                if(documentsResource != null){
                    ddh.deleteResource(documentsResource);
                }
                createDocumentsFolder();
            }
        }
    };

    private void createImagesFolder() {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(IMAGE_FOLDER_NAME).build();
        rootFolder.createFolder(getGoogleApiClient(), changeSet)
                .setResultCallback(imageFolderCallback);
    }

    final ResultCallback<DriveFolderResult> imageFolderCallback = new
            ResultCallback<DriveFolderResult>() {
                @Override
                public void onResult(DriveFolderResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Problem while trying to create a folder");
                        return;
                    }
                    imagesFolder = result.getDriveFolder();
                    imagesFolder.getMetadata(getGoogleApiClient()).setResultCallback(metadataCallback);
                }
            };


    private void createVideosFolder() {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(VIDEO_FOLDER_NAME).build();
        rootFolder.createFolder(getGoogleApiClient(), changeSet)
                .setResultCallback(videosFolderCallback);
    }

    final ResultCallback<DriveFolderResult> videosFolderCallback = new
            ResultCallback<DriveFolderResult>() {
                @Override
                public void onResult(DriveFolderResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Problem while trying to create a folder");
                        return;
                    }
                    videosFolder = result.getDriveFolder();
                    videosFolder.getMetadata(getGoogleApiClient()).setResultCallback(metadataCallback);
                }
            };

    private void createDocumentsFolder() {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(DOCUMENT_FOLDER_NAME).build();
        rootFolder.createFolder(getGoogleApiClient(), changeSet)
                .setResultCallback(documentsFolderCallback);
    }

    final ResultCallback<DriveFolderResult> documentsFolderCallback = new
            ResultCallback<DriveFolderResult>() {
                @Override
                public void onResult(DriveFolderResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Problem while trying to create a folder");
                        return;
                    }
                    documentsFolder = result.getDriveFolder();
                    documentsFolder.getMetadata(getGoogleApiClient()).setResultCallback(metadataCallback);
                }
            };

    ResultCallback<MetadataResult> metadataCallback = new
            ResultCallback<MetadataResult>() {
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
                    dr.setName(metadata.getOriginalFilename());
                    ddh.insertResourceLocally(dr);

                    if(areFoldersCreated()){
                        finish();
                        // Start a new intent.
                        Intent intent = new Intent(CreateFolderStructureActivity.this, AreaAddResourcesActivity.class);
                        intent.putExtra("area_uid", ae.getUniqueId());
                        startActivity(intent);
                    }
                }
            };

    private boolean areFoldersCreated(){
        if((rootFolder != null) && (documentsFolder != null)
                && (imagesFolder != null) && (videosFolder != null)){
            return true;
        }else {
            return false;
        }
    }

    private boolean isRootFolderCreated(){
        if(rootFolder != null){
            return true;
        }else {
            return false;
        }
    }

    private boolean isDocumentsFolderCreated(){
        if(documentsFolder != null){
            return true;
        }else {
            return false;
        }
    }

    private boolean isImagesFolderCreated(){
        if(imagesFolder != null){
            return true;
        }else {
            return false;
        }
    }

    private boolean isVideosFolderCreated(){
        if(videosFolder != null){
            return true;
        }else {
            return false;
        }
    }

}
