package lm.pkp.com.landmap.sync;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

import lm.pkp.com.landmap.AreaDashboardActivity;
import lm.pkp.com.landmap.area.AreaDBHelper;
import lm.pkp.com.landmap.area.UserAreaLoadTask;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.user.UserContext;

/**
 * Created by USER on 11/4/2017.
 */
public class LocalDataRefresher implements AsyncTaskCallback {

    private Context ctxt = null;
    private AsyncTaskCallback callback = null;

    public LocalDataRefresher(Context context, AsyncTaskCallback caller) {
        ctxt = context;
        callback = caller;
    }

    public void refreshLocalData() {

        AreaDBHelper adh = new AreaDBHelper(ctxt);
        adh.deleteAreasLocally();

        PositionsDBHelper pdh = new PositionsDBHelper(ctxt);
        pdh.deletePositionsLocally();

        DriveDBHelper ddh = new DriveDBHelper(ctxt);
        ddh.deleteDriveElementsLocally();

        UserAreaLoadTask loadTask = new UserAreaLoadTask(ctxt);
        loadTask.setCompletionCallback(this);

        try {
            JSONObject queryObj = new JSONObject();
            queryObj.put("us", UserContext.getInstance().getUserElement().getEmail());
            loadTask.execute(queryObj);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void taskCompleted(Object result) {
        callback.taskCompleted(result);
    }
}
