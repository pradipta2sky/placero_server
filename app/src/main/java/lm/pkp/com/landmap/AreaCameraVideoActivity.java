package lm.pkp.com.landmap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.sync.LocalFolderStructureManager;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.util.FileUtil;

/**
 * Created by USER on 11/1/2017.
 */
public class AreaCameraVideoActivity extends Activity {

    // LogCat tag
    private static final String TAG = AreaCameraVideoActivity.class.getSimpleName();


    // Camera activity request codes
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Uri fileUri; // file url to store image/video

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordVideo();
    }


    /**
     * Launching camera app to record video
     */
    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
        // name

        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    /**
     * Receiving activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                File videoFile = new File(fileUri.getPath());
                final AreaContext areaContext = AreaContext.getInstance();
                AreaElement ae = areaContext.getAreaElement();

                DriveResource dr = new DriveResource();
                dr.setName(videoFile.getName());
                dr.setPath(fileUri.getPath());
                dr.setType("file");
                dr.setUserId(UserContext.getInstance().getUserElement().getEmail());
                dr.setSize(videoFile.length() + "");
                dr.setUniqueId(UUID.randomUUID().toString());
                dr.setAreaId(ae.getUniqueId());
                dr.setMimeType(FileUtil.getMimeType(videoFile));
                dr.setContentType("Video");
                String containerDriveId = areaContext.getImagesRootDriveResource().getDriveId();
                dr.setContainerDriveId(containerDriveId);

                AreaContext.getInstance().addNewDriveResource(dr);

                Intent i = new Intent(AreaCameraVideoActivity.this, AreaAddResourcesActivity.class);
                startActivity(i);
            } else if (resultCode == RESULT_CANCELED) {
                // Cancelled case
                finish();
            } else {
                // Failed case
                finish();
            }
        }
    }

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile = new File(LocalFolderStructureManager.getVideoStorageDir().getPath()
                + File.separator + "VID_" + timeStamp + ".mp4");
        return mediaFile;
    }
}
