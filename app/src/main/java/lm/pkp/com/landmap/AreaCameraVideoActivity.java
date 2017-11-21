package lm.pkp.com.landmap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.custom.LocationPositionReceiver;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.provider.GPSLocationProvider;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.util.FileUtil;

/**
 * Created by USER on 11/1/2017.
 */
public class AreaCameraVideoActivity extends Activity implements LocationPositionReceiver {

    // LogCat tag
    private static final String TAG = AreaCameraVideoActivity.class.getSimpleName();


    // Camera activity request codes
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Uri fileUri; // file url to store image/video
    private final DriveResource videoResource = new DriveResource();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);
        this.startPositioning();
        this.recordVideo();
    }

    private void startPositioning() {
        new GPSLocationProvider(this, this, 60).getLocation();
    }

    /**
     * Launching camera app to record video
     */
    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        this.fileUri = this.getOutputMediaFileUri(AreaCameraVideoActivity.MEDIA_TYPE_VIDEO);

        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, this.fileUri); // set the image file
        // name

        // start the video capture Intent
        this.startActivityForResult(intent, AreaCameraVideoActivity.CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
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
        outState.putParcelable("file_uri", this.fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        this.fileUri = savedInstanceState.getParcelable("file_uri");
    }

    /**
     * Receiving activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == AreaCameraVideoActivity.CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File videoFile = new File(this.fileUri.getPath());
                AreaContext areaContext = AreaContext.INSTANCE;
                AreaElement ae = areaContext.getAreaElement();

                videoResource.setName(videoFile.getName());
                videoResource.setPath(videoFile.getAbsolutePath());
                videoResource.setType("file");
                videoResource.setUserId(UserContext.getInstance().getUserElement().getEmail());
                videoResource.setSize(videoFile.length() + "");
                videoResource.setUniqueId(UUID.randomUUID().toString());
                videoResource.setAreaId(ae.getUniqueId());
                videoResource.setMimeType(FileUtil.getMimeType(videoFile));
                videoResource.setContentType("Video");
                videoResource.setContainerId(areaContext.getVideosRootDriveResource().getResourceId());
                videoResource.setCreatedOnMillis(System.currentTimeMillis() + "");

                areaContext.addResourceToQueue(videoResource);

                Intent i = new Intent(this, AreaAddResourcesActivity.class);
                this.startActivity(i);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Cancelled case
                this.finish();
            } else {
                // Failed case
                this.finish();
            }
        }
    }

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(AreaCameraVideoActivity.getOutputMediaFile());
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile() {
        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File localRoot = AreaContext.INSTANCE.getAreaLocalVideoRoot(areaElement.getUniqueId());
        return new File(localRoot + File.separator + "VID_" + timeStamp + ".mp4");
    }

    @Override
    public void receivedLocationPostion(PositionElement pe) {
        this.videoResource.setLatitude(pe.getLat() + "");
        this.videoResource.setLongitude(pe.getLon() + "");
    }

    @Override
    public void locationFixTimedOut() {

    }

    @Override
    public void providerDisabled() {

    }
}
