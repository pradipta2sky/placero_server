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

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Stack;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.custom.ApiClientAsyncTask;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
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
        for (int i = 0; i < resources.size(); i++) {
            DriveResource dr = resources.get(i);
            if (!dr.getType().equalsIgnoreCase("folder")) {
                processStack.push(dr);
            }
        }

        processResources();
    }

    private void processResources() {
        if (!processStack.isEmpty()) {
            DriveResource res = processStack.pop();
            processResource(res);
        } else {
            finish();
            Intent addResourcesIntent = new Intent(UploadResourcesActivity.this, AreaAddResourcesActivity.class);
            startActivity(addResourcesIntent);
        }
    }

    private void processResource(DriveResource res) {
        new FileProcessingTask(res).execute();
    }

    private class FileProcessingTask extends AsyncTask {

        private DriveResource resource = null;

        public FileProcessingTask(DriveResource dr) {
            resource = dr;
        }

        @Override
        protected Object doInBackground(Object[] params) {

            DriveId folderId = DriveId.decodeFromString(resource.getContainerDriveId());
            DriveIdResult idResult = Drive.DriveApi.fetchDriveId(getGoogleApiClient(), folderId.getResourceId()).await();
            DriveFolder folder = idResult.getDriveId().asDriveFolder();

            DriveContentsResult contentsResult = Drive.DriveApi.newDriveContents(getGoogleApiClient()).await();
            DriveContents contents = contentsResult.getDriveContents();
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(resource.getName())
                    .setMimeType(resource.getMimeType())
                    .build();
            DriveFileResult driveFileResult = folder.createFile(getGoogleApiClient(), changeSet, contents).await();

            DriveFile createdFile = driveFileResult.getDriveFile();
            createdFile.addChangeListener(getGoogleApiClient(), new FileMetaChangeListener(resource, createdFile));

            return null;
        }
    }

    private class FileMetaChangeListener implements ChangeListener {

        private DriveResource resource = null;
        private DriveFile driveFile = null;

        public FileMetaChangeListener(DriveResource res, DriveFile dFile) {
            resource = res;
            driveFile = dFile;
        }

        @Override
        public void onChange(ChangeEvent changeEvent) {
            if (changeEvent.hasMetadataChanged()) {
                DriveId driveId = changeEvent.getDriveId();
                resource.setDriveId(driveId.toString());
                resource.setDriveResourceId(driveId.getResourceId());

                DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
                ddh.insertResourceLocally(resource);
                ddh.insertResourceToServer(resource);

                driveFile.removeChangeListener(getGoogleApiClient(), this);
                new CopyContentsAsyncTask(getApplicationContext(), resource).execute(driveFile);
            }
        }
    }

    public class CopyContentsAsyncTask extends ApiClientAsyncTask<DriveFile, Void, Boolean> {
        private DriveResource localRes = null;

        public CopyContentsAsyncTask(Context context, DriveResource res) {
            super(context);
            localRes = res;
        }

        @Override
        protected Boolean doInBackgroundConnected(DriveFile... args) {
            final DriveFile file = args[0];
            DriveContentsResult driveContentsResult = file.open(
                    getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null).await();
            if (!driveContentsResult.getStatus().isSuccess()) {
                return false;
            }

            DriveContents driveContents = driveContentsResult.getDriveContents();
            OutputStream outputStream = driveContents.getOutputStream();
            try {
                File inputFile = new File(localRes.getPath());
                FileInputStream fileInputStream = new FileInputStream(inputFile);
                IOUtils.copyLarge(fileInputStream, outputStream);
                IOUtils.closeQuietly(fileInputStream);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            driveContents.commit(getGoogleApiClient(), null).await();
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            AreaContext.getInstance().removeUploadedDriveResource(localRes);
            AreaContext.getInstance().getAreaElement().getDriveResources().add(localRes);
            processResources();
        }
    }


}
