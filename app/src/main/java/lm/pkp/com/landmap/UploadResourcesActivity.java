/**
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package lm.pkp.com.landmap;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.Override;
import java.util.ArrayList;
import java.util.Stack;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.custom.ApiClientAsyncTask;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.google.drive.BaseDriveActivity;
import lm.pkp.com.landmap.util.FileUtil;


/**
 * An activity to create a file inside a folder.
 */
public class UploadResourcesActivity extends BaseDriveActivity {

    private Stack<DriveResource> processStack = new Stack<>();


    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);

        // Preprocessing of the resources.
        AreaContext ac = AreaContext.getInstance();
        ArrayList<DriveResource> resources = ac.getUploadedDriveResources();
        for(int i = 0; i < resources.size(); i++){
            DriveResource dr = resources.get(i);
            if(!dr.getType().equalsIgnoreCase("folder")){
                File localFile = new File(dr.getPath());
                if(FileUtil.isImageFile(localFile)){
                    dr.setContentType("Image");
                    dr.setContainerDriveId(ac.getImagesRootDriveResource().getDriveId());
                }else if(FileUtil.isVideoFile(localFile)){
                    dr.setContentType("Video");
                    dr.setContainerDriveId(ac.getVideosRootDriveResource().getDriveId());
                }else {
                    dr.setContentType("Document");
                    dr.setContainerDriveId(ac.getDocumentRootDriveResource().getDriveId());
                }
                dr.setMimeType(FileUtil.getMimeType(localFile));
            }
            processStack.push(dr);
        }

        // Process the resources now.
        processResources();
    }

    private void processResources() {
        if (!processStack.isEmpty()) {
            DriveResource res = processStack.pop();
            processResource(res);
        }else {
            finish();
            Intent addResourcesIntent = new Intent(UploadResourcesActivity.this, AreaAddResourcesActivity.class);
            startActivity(addResourcesIntent);
        }
    }

    private void processResource(DriveResource res) {
        DriveId folderId = DriveId.decodeFromString(res.getContainerDriveId());
        Drive.DriveApi.fetchDriveId(getGoogleApiClient(), folderId.getResourceId())
                .setResultCallback(new ContainerIdCallback(res));
    }

    private class ContainerIdCallback implements ResultCallback<DriveIdResult>{

        private DriveResource localRes = null;

        public ContainerIdCallback(DriveResource res){
            localRes = res;
        }

        @Override
        public void onResult(DriveIdResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Cannot find DriveId. Are you authorized to view this file?");
                return;
            }
            Drive.DriveApi.newDriveContents(getGoogleApiClient())
                    .setResultCallback(new ContainerContentsCallback(localRes));
        }
    }

    private class ContainerContentsCallback implements ResultCallback<DriveContentsResult>{

        private DriveResource localRes = null;

        public ContainerContentsCallback(DriveResource res){
            localRes = res;
        }

        @Override
        public void onResult(DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create new file contents");
                return;
            }
            DriveId folderDriveId = DriveId.decodeFromString(localRes.getContainerDriveId());
            DriveFolder folder = folderDriveId.asDriveFolder();
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(localRes.getName())
                    .setMimeType(localRes.getMimeType())
                    .build();
            folder.createFile(getGoogleApiClient(), changeSet, result.getDriveContents())
                    .setResultCallback(new FileCreationCallback(localRes));
        }
    }


    private class FileCreationCallback implements ResultCallback<DriveFileResult>{

        private DriveResource localRes = null;

        public FileCreationCallback(DriveResource res){
            localRes = res;
        }

        @Override
        public void onResult(DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Error while trying to create the file");
                return;
            }
            DriveFile resFile = result.getDriveFile();
            localRes.setDriveId(resFile.getDriveId().toString());
            new EditContentsAsyncTask(getApplicationContext(), localRes).execute(resFile);
        }
    }

    public class EditContentsAsyncTask extends ApiClientAsyncTask<DriveFile, Void, Boolean> {
        private DriveResource localRes = null;

        public EditContentsAsyncTask(Context context, DriveResource res) {
            super(context);
            localRes = res;
        }

        @Override
        protected Boolean doInBackgroundConnected(DriveFile... args) {
            DriveFile file = args[0];
            try {
                DriveContentsResult driveContentsResult = file.open(
                        getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null).await();
                if (!driveContentsResult.getStatus().isSuccess()) {
                    return false;
                }
                DriveContents driveContents = driveContentsResult.getDriveContents();

                OutputStream outputStream = driveContents.getOutputStream();
                outputStream.write(FileUtils.readFileToByteArray(new File(localRes.getPath())));
                outputStream.flush();
                outputStream.close();

                com.google.android.gms.common.api.Status status =
                        driveContents.commit(getGoogleApiClient(), null).await();

                AreaContext.getInstance().removeUploadedDriveResource(localRes);

                DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
                ddh.insertResourceLocally(localRes);
                ddh.insertResourceToServer(localRes);

                return status.getStatus().isSuccess();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                showMessage("Error while editing contents");
                return;
            }
            processResources();
        }
    }


}
