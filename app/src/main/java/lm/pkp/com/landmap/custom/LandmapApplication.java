package lm.pkp.com.landmap.custom;

import android.app.Application;
import android.content.Context;

/**
 * Created by USER on 11/10/2017.
 */
public class LandmapApplication extends Application {

    public static LandmapApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        LandmapApplication.instance = this;
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    public static LandmapApplication getInstance() {
        return LandmapApplication.instance;
    }
}
