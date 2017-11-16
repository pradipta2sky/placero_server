package lm.pkp.com.landmap.sync;

import android.os.Environment;
import android.util.Log;

import java.io.File;

import lm.pkp.com.landmap.area.FileStorageConstants;

/**
 * Created by USER on 11/5/2017.
 */
public class LocalFolderStructureManager {

    private static final String TAG = LocalFolderStructureManager.class.getSimpleName();

    private static File tempStorageDir = null;
    private static File docsStorageDir = null;
    private static File videoStorageDir = null;
    private static File imageStorageDir = null;

    public static void create() {
        createImagesFolder();
        createVideosFolder();
        createDocumentsFolder();
        createTempFolder();
    }

    private static void createTempFolder() {
        // External sdcard location
        tempStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                FileStorageConstants.TEMP_ROOT_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!tempStorageDir.exists()) {
            if (!tempStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create " + FileStorageConstants.TEMP_ROOT_FOLDER_NAME + " directory");
            }
        }

    }

    private static void createDocumentsFolder() {
        // External sdcard location
        docsStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                FileStorageConstants.DOCUMENT_ROOT_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!docsStorageDir.exists()) {
            if (!docsStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create " + FileStorageConstants.DOCUMENT_ROOT_FOLDER_NAME + " directory");
            }
        }
    }

    private static void createVideosFolder() {
        // External sdcard location
        videoStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                FileStorageConstants.VIDEO_ROOT_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!videoStorageDir.exists()) {
            if (!videoStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create " + FileStorageConstants.VIDEO_ROOT_FOLDER_NAME + " directory");
            }
        }
    }

    private static void createImagesFolder() {
        // External sdcard location
        imageStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                FileStorageConstants.IMAGE_ROOT_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!imageStorageDir.exists()) {
            if (!imageStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create " + FileStorageConstants.IMAGE_ROOT_FOLDER_NAME + " directory");
            }
        }
    }

    public static File getTempStorageDir() {
        return tempStorageDir;
    }

    public static File getDocsStorageDir() {
        return docsStorageDir;
    }

    public static File getVideoStorageDir() {
        return videoStorageDir;
    }

    public static File getImageStorageDir() {
        return imageStorageDir;
    }

    public static File getLocalFolderByMimeType(String mimeType) {
        if (mimeType != null && mimeType.startsWith("image")) {
            return imageStorageDir;
        } else if (mimeType != null && mimeType.startsWith("video")) {
            return videoStorageDir;
        } else {
            return docsStorageDir;
        }
    }
}
