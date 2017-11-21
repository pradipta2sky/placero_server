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

    private static File tempStorageDir;
    private static File docsStorageDir;
    private static File videoStorageDir;
    private static File imageStorageDir;

    public static void create() {
        LocalFolderStructureManager.createImagesFolder();
        LocalFolderStructureManager.createVideosFolder();
        LocalFolderStructureManager.createDocumentsFolder();
        LocalFolderStructureManager.createTempFolder();
    }

    private static void createTempFolder() {
        // External sdcard location
        LocalFolderStructureManager.tempStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                FileStorageConstants.TEMP_ROOT_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!LocalFolderStructureManager.tempStorageDir.exists()) {
            if (!LocalFolderStructureManager.tempStorageDir.mkdirs()) {
                Log.d(LocalFolderStructureManager.TAG, "Oops! Failed create " + FileStorageConstants.TEMP_ROOT_FOLDER_NAME + " directory");
            }
        }

    }

    private static void createDocumentsFolder() {
        // External sdcard location
        LocalFolderStructureManager.docsStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                FileStorageConstants.DOCUMENT_ROOT_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!LocalFolderStructureManager.docsStorageDir.exists()) {
            if (!LocalFolderStructureManager.docsStorageDir.mkdirs()) {
                Log.d(LocalFolderStructureManager.TAG, "Oops! Failed create " + FileStorageConstants.DOCUMENT_ROOT_FOLDER_NAME + " directory");
            }
        }
    }

    private static void createVideosFolder() {
        // External sdcard location
        LocalFolderStructureManager.videoStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                FileStorageConstants.VIDEO_ROOT_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!LocalFolderStructureManager.videoStorageDir.exists()) {
            if (!LocalFolderStructureManager.videoStorageDir.mkdirs()) {
                Log.d(LocalFolderStructureManager.TAG, "Oops! Failed create " + FileStorageConstants.VIDEO_ROOT_FOLDER_NAME + " directory");
            }
        }
    }

    private static void createImagesFolder() {
        // External sdcard location
        LocalFolderStructureManager.imageStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                FileStorageConstants.IMAGE_ROOT_FOLDER_NAME);
        // Create the storage directory if it does not exist
        if (!LocalFolderStructureManager.imageStorageDir.exists()) {
            if (!LocalFolderStructureManager.imageStorageDir.mkdirs()) {
                Log.d(LocalFolderStructureManager.TAG, "Oops! Failed create " + FileStorageConstants.IMAGE_ROOT_FOLDER_NAME + " directory");
            }
        }
    }

    public static File getTempStorageDir() {
        return LocalFolderStructureManager.tempStorageDir;
    }

    public static File getDocsStorageDir() {
        return LocalFolderStructureManager.docsStorageDir;
    }

    public static File getVideoStorageDir() {
        return LocalFolderStructureManager.videoStorageDir;
    }

    public static File getImageStorageDir() {
        return LocalFolderStructureManager.imageStorageDir;
    }

    public static File getLocalFolderByMimeType(String mimeType) {
        if (mimeType != null && mimeType.startsWith("image")) {
            return LocalFolderStructureManager.imageStorageDir;
        } else if (mimeType != null && mimeType.startsWith("video")) {
            return LocalFolderStructureManager.videoStorageDir;
        } else {
            return LocalFolderStructureManager.docsStorageDir;
        }
    }
}
