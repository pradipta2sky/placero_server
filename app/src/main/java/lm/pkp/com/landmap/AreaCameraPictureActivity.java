package lm.pkp.com.landmap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.iceteck.silicompressorr.SiliCompressor;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
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
public class AreaCameraPictureActivity extends Activity implements LocationPositionReceiver {

    // LogCat tag
    private static final String TAG = AreaCameraPictureActivity.class.getSimpleName();


    // Camera activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;

    private Uri fileUri; // file url to store image/video
    private final DriveResource pictureResource = new DriveResource();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);
        this.startPositioning();
        this.captureImage();
    }

    private void startPositioning() {
        new GPSLocationProvider(this, this, 30).getLocation();
    }

    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        this.fileUri = this.getOutputMediaFileUri(AreaCameraPictureActivity.MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, this.fileUri);
        this.startActivityForResult(intent, AreaCameraPictureActivity.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("file_uri", this.fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.fileUri = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == AreaCameraPictureActivity.CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File imageFile = new File(this.fileUri.getPath());
                AreaContext areaContext = AreaContext.INSTANCE;
                AreaElement ae = areaContext.getAreaElement();

                SiliCompressor compressor = SiliCompressor.with(this.getApplicationContext());
                String compressedFilePath = compressor.compress(imageFile.getAbsolutePath(),
                        areaContext.getAreaLocalPictureThumbnailRoot(ae.getUniqueId()), true);

                File compressedFile = new File(compressedFilePath);
                File loadableFile = AreaCameraPictureActivity.getOutputMediaFile();
                try {
                    FileUtils.copyFile(compressedFile, loadableFile);
                    compressedFile.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                this.pictureResource.setName(loadableFile.getName());
                this.pictureResource.setPath(loadableFile.getAbsolutePath());
                this.pictureResource.setType("file");
                this.pictureResource.setUserId(UserContext.getInstance().getUserElement().getEmail());
                this.pictureResource.setSize(loadableFile.length() + "");
                this.pictureResource.setUniqueId(UUID.randomUUID().toString());
                this.pictureResource.setAreaId(ae.getUniqueId());
                this.pictureResource.setMimeType(FileUtil.getMimeType(loadableFile));
                this.pictureResource.setContentType("Image");
                this.pictureResource.setContainerId(areaContext.getImagesRootDriveResource().getResourceId());

                areaContext.addResourceToQueue(this.pictureResource);

                Intent i = new Intent(this, AreaAddResourcesActivity.class);
                this.startActivity(i);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Cancelled case
                this.finish();
            } else {
                // Error case
                this.finish();
            }
        }
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(AreaCameraPictureActivity.getOutputMediaFile());
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile() {
        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File localRoot = AreaContext.INSTANCE.getAreaLocalImageRoot(areaElement.getUniqueId());
        return new File(localRoot.getAbsolutePath() + File.separator + "IMG_" + timeStamp + ".jpg");
    }

    @Override
    public void receivedLocationPostion(PositionElement pe) {
        this.pictureResource.setLatitude(pe.getLat() + "");
        this.pictureResource.setLongitude(pe.getLon() + "");
    }

    @Override
    public void locationFixTimedOut() {

    }

    @Override
    public void providerDisabled() {

    }

}
