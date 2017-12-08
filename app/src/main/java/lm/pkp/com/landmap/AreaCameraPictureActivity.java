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
import lm.pkp.com.landmap.area.model.AreaElement;
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


    // Camera activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    private Uri fileUri; // file url to store image/video
    private final DriveResource pictureResource = new DriveResource();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);
        startPositioning();
        captureImage();
    }

    private void startPositioning() {
        new GPSLocationProvider(this, this, 30).getLocation();
    }

    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File imageFile = new File(fileUri.getPath());
                AreaContext areaContext = AreaContext.INSTANCE;
                AreaElement ae = areaContext.getAreaElement();

                SiliCompressor compressor = SiliCompressor.with(getApplicationContext());
                String compressedFilePath = compressor.compress(imageFile.getAbsolutePath(),
                        areaContext.getAreaLocalPictureThumbnailRoot(ae.getUniqueId()), true);

                File compressedFile = new File(compressedFilePath);
                File loadableFile = getOutputMediaFile();
                try {
                    FileUtils.copyFile(compressedFile, loadableFile);
                    compressedFile.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                pictureResource.setName(loadableFile.getName());
                pictureResource.setPath(loadableFile.getAbsolutePath());
                pictureResource.setType("file");
                pictureResource.setUserId(UserContext.getInstance().getUserElement().getEmail());
                pictureResource.setSize(loadableFile.length() + "");
                pictureResource.setUniqueId(UUID.randomUUID().toString());
                pictureResource.setAreaId(ae.getUniqueId());
                pictureResource.setMimeType(FileUtil.getMimeType(loadableFile));
                pictureResource.setContentType("Image");
                pictureResource.setContainerId(areaContext.getImagesRootDriveResource().getResourceId());
                pictureResource.setCreatedOnMillis(System.currentTimeMillis() + "");

                areaContext.addResourceToQueue(pictureResource);

                Intent i = new Intent(this, AreaAddResourcesActivity.class);
                startActivity(i);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Cancelled case
                finish();
            } else {
                // Error case
                finish();
            }
        }
    }

    public Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
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
        pictureResource.setLatitude(pe.getLat() + "");
        pictureResource.setLongitude(pe.getLon() + "");
    }

    @Override
    public void locationFixTimedOut() {

    }

    @Override
    public void providerDisabled() {

    }

}
