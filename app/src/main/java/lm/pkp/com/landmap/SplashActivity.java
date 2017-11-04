package lm.pkp.com.landmap;

/**
 * Created by USER on 10/27/2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONObject;

import lm.pkp.com.landmap.area.AreaDBHelper;
import lm.pkp.com.landmap.area.UserAreaLoadTask;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.sync.LocalDataRefresher;
import lm.pkp.com.landmap.user.UserContext;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new LocalDataRefresher(getApplicationContext(), new DataReloadCallback()).refreshLocalData();
    }

    private class DataReloadCallback implements AsyncTaskCallback{

        @Override
        public void taskCompleted(Object result) {
            Intent areaDashboardIntent = new Intent(SplashActivity.this, AreaDashboardActivity.class);
            startActivity(areaDashboardIntent);
        }
    }

}