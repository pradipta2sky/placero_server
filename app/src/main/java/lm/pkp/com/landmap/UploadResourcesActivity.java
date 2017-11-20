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
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.custom.ApiClientAsyncTask;
import lm.pkp.com.landmap.custom.ThumbnailCreator;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.util.FileUtil;


/**
 * An activity to create a file inside a folder.
 */
public class UploadResourcesActivity extends BaseDriveActivity {

    private Stack<DriveResource> processStack = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_area_resources);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);

        // Preprocessing of the resources.
        ArrayList<DriveResource> resources = AreaContext.INSTANCE.getUploadedQueue();
        for (int i = 0; i < resources.size(); i++) {
            processStack.push(resources.get(i));
        }

        processResources();
    }

    private void processResources() {
        if (!processStack.isEmpty()) {
            processResource(processStack.pop());
        } else {
            finish();
            getGoogleApiClient().disconnect();
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

            DriveIdResult idResult
                    = Drive.DriveApi.fetchDriveId(getGoogleApiClient(), resource.getContainerId()).await();
            DriveFolder folder = idResult.getDriveId().asDriveFolder();

            DriveContentsResult contentsResult
                    = Drive.DriveApi.newDriveContents(getGoogleApiClient()).await();
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
                resource.setResourceId(driveId.getResourceId());

                DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
                ddh.insertResourceLocally(resource);
                ddh.insertResourceToServer(resource);

                driveFile.removeChangeListener(getGoogleApiClient(), this);
                new CopyContentsAsyncTask(getApplicationContext(), resource).execute(driveFile);
            }
        }
    }

    public class CopyContentsAsyncTask extends ApiClientAsyncTask<DriveFile, Void, Boolean> {
        private DriveResource resource = null;

        public CopyContentsAsyncTask(Context context, DriveResource resource) {
            super(context);
            this.resource = resource;
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
                File inputFile = new File(resource.getPath());
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

            AreaContext.INSTANCE.removeResourceFromQueue(resource);
            AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
            areaElement.getMediaResources().add(resource);

            // Create thumbnails of the uploaded files for display.
            String resourcePath = resource.getPath();
            File resourceFile = new File(resourcePath);

            ThumbnailCreator tCreator = new ThumbnailCreator(getApplicationContext());
            if(FileUtil.isImageFile(resourceFile)){
                tCreator.createImageThumbnail(resourceFile, areaElement.getUniqueId());
            }else if(FileUtil.isVideoFile(resourceFile)){
                tCreator.createVideoThumbnail(resourceFile, areaElement.getUniqueId());
            }else {
                tCreator.createDocumentThumbnail(resourceFile, areaElement.getUniqueId());
            }

            processResources();
        }
    }
}
