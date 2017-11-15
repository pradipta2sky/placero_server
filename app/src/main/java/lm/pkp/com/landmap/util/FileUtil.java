package lm.pkp.com.landmap.util;

import java.io.File;
import java.net.URLConnection;

/**
 * Created by USER on 11/3/2017.
 */
public class FileUtil {

    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    public static boolean isImageFile(File file) {
        String path = file.getAbsolutePath();
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public static boolean isVideoFile(File file) {
        String path = file.getAbsolutePath();
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public static String getMimeType(File file) {
        String path = file.getAbsolutePath();
        return URLConnection.guessContentTypeFromName(path);
    }
}
