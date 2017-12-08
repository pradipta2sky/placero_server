package lm.pkp.com.landmap;

/**
 * Created by USER on 10/27/2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.sync.LocalDataRefresher;
import lm.pkp.com.landmap.sync.LocalFolderStructureManager;

public class SplashActivity extends Activity {

    private boolean userExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);
        setContentView(layout.activity_splash);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            userExists = new Boolean(extras.getString("user_exists"));
        }

        new LocalDataRefresher(getApplicationContext(), new DataReloadCallback()).refreshLocalData();
        LocalFolderStructureManager.create();
    }

    private class DataReloadCallback implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            if(userExists){
                Intent dashboardIntent = new Intent(SplashActivity.this, AreaDashboardActivity.class);
                startActivity(dashboardIntent);
            }else {
                Intent commonFoldersIntent = new Intent(SplashActivity.this, CreateCommonFolderStructureActivity.class);
                startActivity(commonFoldersIntent);
            }
            finish();
        }
    }

}