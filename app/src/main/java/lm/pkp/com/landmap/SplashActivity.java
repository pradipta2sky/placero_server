package lm.pkp.com.landmap;

/**
 * Created by USER on 10/27/2017.
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import org.json.JSONObject;

import lm.pkp.com.landmap.area.AreaDBHelper;
import lm.pkp.com.landmap.area.UserAreaLoadTask;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.user.UserContext;

public class SplashActivity extends Activity implements AsyncTaskCallback{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AreaDBHelper adh = new AreaDBHelper(getApplicationContext());
        adh.deleteAreasLocally();

        PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
        pdh.deletePositionsLocally();

        UserAreaLoadTask loadTask = new UserAreaLoadTask(getApplicationContext());
        loadTask.setCompletionCallback(this);
        JSONObject queryObj = new JSONObject();
        try {
            queryObj.put("us", UserContext.getInstance().getUserElement().getEmail());
            loadTask.execute(queryObj);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void taskCompleted(Object result) {
        Intent areaDashboardIntent = new Intent(SplashActivity.this, AreaDashboardActivity.class);
        startActivity(areaDashboardIntent);
    }
}