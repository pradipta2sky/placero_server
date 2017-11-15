package lm.pkp.com.landmap.util;

import java.lang.reflect.Method;

/**
 * Created by USER on 10/24/2017.
 */
public class AndroidSystemUtil {

    public static final String getDeviceId() {
        String deviceID = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            deviceID = (String) (get.invoke(c, "ro.serialno", "unknown"));
            return deviceID;
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return deviceID;
    }
}
