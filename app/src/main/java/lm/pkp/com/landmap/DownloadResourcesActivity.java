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

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.custom.ThumbnailCreator;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.sync.LocalFolderStructureManager;
import lm.pkp.com.landmap.util.FileUtil;


/**
 * An activity to create a file inside a folder.
 */
public class DownloadResourcesActivity extends BaseDriveActivity {

    private Stack<DriveResource> processStack = new Stack<>();

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);

        // Preprocessing of the resources.
        AreaDBHelper adh = new AreaDBHelper(getApplicationContext());

        ArrayList<AreaElement> areas = adh.getAllAreas();
        AreaContext ac = AreaContext.INSTANCE;
        for (int i = 0; i < areas.size(); i++) {
            AreaElement areaElement = areas.get(i);
            ac.setAreaElement(areaElement, getApplicationContext());

            List<DriveResource> resources = ac.getAreaElement().getDriveResources();
            for (int j = 0; j < resources.size(); j++) {
                DriveResource dr = resources.get(j);
                if (!dr.getType().equalsIgnoreCase("folder")) {
                    processStack.push(dr);
                }
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

            Intent areaDashboardIntent = new Intent(getApplicationContext(), AreaDashboardActivity.class);
            startActivity(areaDashboardIntent);
        }
    }

    private void processResource(DriveResource res) {
        new ResourceDownloadTask(res).execute();
    }

    private class ResourceDownloadTask extends AsyncTask {
        private DriveResource resource = null;

        public ResourceDownloadTask(DriveResource dr) {
            resource = dr;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            DriveIdResult driveIdResult = Drive.DriveApi.fetchDriveId(getGoogleApiClient(),
                    resource.getResourceId()).await();
            DriveFile driveFile = driveIdResult.getDriveId().asDriveFile();
            try {
                DriveContentsResult driveContentsResult =
                        driveFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
                if (!driveContentsResult.getStatus().isSuccess()) {
                    return false;
                }
                DriveContents driveContents = driveContentsResult.getDriveContents();
                String contentType = resource.getContentType();

                File localStoreRoot = null;
                if(contentType.equalsIgnoreCase("Image")){
                    localStoreRoot = AreaContext.INSTANCE.getAreaLocalImageRoot(resource.getAreaId());
                }else if(contentType.equalsIgnoreCase("Video")){
                    localStoreRoot = AreaContext.INSTANCE.getAreaLocalVideoRoot(resource.getAreaId());
                }else {
                    localStoreRoot = AreaContext.INSTANCE.getAreaLocalDocumentRoot(resource.getAreaId());
                }

                String storeFilePath = localStoreRoot.getAbsolutePath()
                        + File.separatorChar + resource.getName();
                File storeFile = new File(storeFilePath);
                if (!storeFile.exists()) {
                    InputStream driveStream = driveContents.getInputStream();
                    OutputStream localStream = new FileOutputStream(storeFile);
                    IOUtils.copyLarge(driveStream,localStream);
                    localStream.flush();
                    localStream.close();
                }

                // Create the thumbnail of the file.
                ThumbnailCreator tCreator = new ThumbnailCreator(getApplicationContext());
                if(contentType.equalsIgnoreCase("Image")){
                    tCreator.createImageThumbnail(storeFile, resource.getAreaId());
                }else if(contentType.equalsIgnoreCase("Video")){
                    tCreator.createVideoThumbnail(storeFile, resource.getAreaId());
                }else {
                    tCreator.createDocumentThumbnail(storeFile, resource.getAreaId());
                }

                processResources();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    }
}
