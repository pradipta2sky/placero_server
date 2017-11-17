package lm.pkp.com.landmap;

/**
 * Created by USER on 10/27/2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.sync.LocalDataRefresher;
import lm.pkp.com.landmap.sync.LocalFolderStructureManager;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(R.layout.activity_splash);

        new LocalDataRefresher(getApplicationContext(), new DataReloadCallback()).refreshLocalData();
        LocalFolderStructureManager.create();
    }

    private class DataReloadCallback implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            Intent commonFoldersIntent = new Intent(SplashActivity.this,
                    CreateCommonFolderStructureActivity.class);
            startActivity(commonFoldersIntent);
            finish();
        }
    }

}