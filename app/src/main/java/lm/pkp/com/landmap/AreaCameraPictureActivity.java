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
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.util.FileUtil;

/**
 * Created by USER on 11/1/2017.
 */
public class AreaCameraPictureActivity extends Activity {

    // LogCat tag
    private static final String TAG = AreaCameraPictureActivity.class.getSimpleName();


    // Camera activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;

    private Uri fileUri; // file url to store image/video

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        captureImage();
    }

    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
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
            if (resultCode == RESULT_OK) {
                File imageFile = new File(fileUri.getPath());
                final AreaContext areaContext = AreaContext.INSTANCE;
                AreaElement ae = areaContext.getAreaElement();

                final SiliCompressor compressor = SiliCompressor.with(getApplicationContext());
                final String compressedFilePath = compressor.compress(imageFile.getAbsolutePath(),
                        areaContext.getAreaLocalPictureThumbnailRoot(ae.getUniqueId()), true);

                File compressedFile = new File(compressedFilePath);
                final File loadableFile = getOutputMediaFile();
                try {
                    FileUtils.copyFile(compressedFile, loadableFile);
                    compressedFile.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                DriveResource uploadResource = new DriveResource();
                uploadResource.setName(loadableFile.getName());
                uploadResource.setPath(loadableFile.getAbsolutePath());
                uploadResource.setType("file");
                uploadResource.setUserId(UserContext.getInstance().getUserElement().getEmail());
                uploadResource.setSize(loadableFile.length() + "");
                uploadResource.setUniqueId(UUID.randomUUID().toString());
                uploadResource.setAreaId(ae.getUniqueId());
                uploadResource.setMimeType(FileUtil.getMimeType(loadableFile));
                uploadResource.setContentType("Image");
                uploadResource.setContainerId(areaContext.getImagesRootDriveResource().getResourceId());

                areaContext.addResourceToQueue(uploadResource);

                Intent i = new Intent(AreaCameraPictureActivity.this, AreaAddResourcesActivity.class);
                startActivity(i);

            } else if (resultCode == RESULT_CANCELED) {
                // Cancelled case
                finish();
            } else {
                // Error case
                finish();
            }
        }
    }

    public Uri getOutputMediaFileUri(int type) {
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
}
