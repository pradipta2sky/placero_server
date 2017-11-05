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
import com.google.android.gms.drive.DriveId;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Stack;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.custom.ApiClientAsyncTask;
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
        AreaContext ac = AreaContext.getInstance();
        List<DriveResource> resources = ac.getAreaElement().getDriveResources();
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
        processResources();
    }

    private void processResources() {
        if (!processStack.isEmpty()) {
            DriveResource res = processStack.pop();
            processResource(res);
        }else {
            finish();
            Intent areaDashboardIntent = new Intent(DownloadResourcesActivity.this, PositionMarkerActivity.class);
            startActivity(areaDashboardIntent);
        }
    }

    private void processResource(DriveResource res) {
        DriveId driveId = DriveId.decodeFromString(res.getDriveId());
        Drive.DriveApi.fetchDriveId(getGoogleApiClient(), driveId.getResourceId())
                .setResultCallback(new ResourceIdCallback(res));
    }

    private class ResourceIdCallback implements ResultCallback<DriveIdResult>{

        private DriveResource localRes = null;

        public ResourceIdCallback(DriveResource res){
            localRes = res;
        }

        @Override
        public void onResult(DriveIdResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Cannot find DriveId. Are you authorized to view this file?");
                return;
            }
            new RetrieveDriveFileContentsAsyncTask(getApplicationContext(),
                    localRes.getMimeType(), localRes.getName()).execute(result.getDriveId());
        }
    }

    public class RetrieveDriveFileContentsAsyncTask extends ApiClientAsyncTask<DriveId, Void, Boolean> {

        private String mimeType = null;
        private String fileName = null;

        public RetrieveDriveFileContentsAsyncTask(Context context, String mt, String fn) {
            super(context);
            mimeType = mt;
            fileName = fn;
        }

        @Override
        protected Boolean doInBackgroundConnected(DriveId... args) {
            try {
                DriveFile file = args[0].asDriveFile();
                DriveContentsResult driveContentsResult =
                        file.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
                if (!driveContentsResult.getStatus().isSuccess()) {
                    return false;
                }
                DriveContents driveContents = driveContentsResult.getDriveContents();

                String localDir = LocalFolderStructureManager.getLocalFolderByMimeType(mimeType).getAbsolutePath();
                File localFile = new File( localDir + File.separatorChar + fileName);

                if(!localFile.exists()){
                    InputStream driveStream = driveContents.getInputStream();
                    OutputStream localStream = new FileOutputStream(localFile);

                    IOUtils.copy(driveStream, localStream);

                    driveStream.close();
                    localStream.flush();
                    localStream.close();
                }

                com.google.android.gms.common.api.Status status =
                        driveContents.commit(getGoogleApiClient(), null).await();

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
