package lm.pkp.com.landmap.sync;

import android.content.Context;

import org.json.JSONObject;

import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.tasks.PublicAreasLoadTask;
import lm.pkp.com.landmap.area.tasks.UserAreaDetailsLoadTask;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.weather.db.WeatherDBHelper;

/**
 * Created by USER on 11/4/2017.
 */
public class LocalDataRefresher implements AsyncTaskCallback {

    private Context ctxt;
    private AsyncTaskCallback callback;

    public LocalDataRefresher(Context context, AsyncTaskCallback caller) {
        this.ctxt = context;
        this.callback = caller;
    }

    public void refreshLocalData() {

        AreaDBHelper adh = new AreaDBHelper(this.ctxt);
        adh.deleteAreasLocally();

        PositionsDBHelper pdh = new PositionsDBHelper(this.ctxt);
        pdh.deletePositionsLocally();

        WeatherDBHelper wdh = new WeatherDBHelper(this.ctxt);
        wdh.deleteWeatherElementsLocally();

        DriveDBHelper ddh = new DriveDBHelper(this.ctxt);
        ddh.deleteDriveElementsLocally();

        UserAreaDetailsLoadTask loadTask = new UserAreaDetailsLoadTask(this.ctxt);
        loadTask.setCompletionCallback(this);

        try {
            JSONObject queryObj = new JSONObject();
            queryObj.put("us", UserContext.getInstance().getUserElement().getEmail());
            loadTask.execute(queryObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshPublicAreas() {
        AreaDBHelper adh = new AreaDBHelper(this.ctxt);
        adh.deletePublicAreas();

        PublicAreasLoadTask loadTask = new PublicAreasLoadTask(this.ctxt);
        loadTask.setCompletionCallback(this);
        try {
            loadTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshPublicAreas(String searchKey) {
        AreaDBHelper adh = new AreaDBHelper(this.ctxt);
        adh.deletePublicAreas();

        PublicAreasLoadTask loadTask = new PublicAreasLoadTask(this.ctxt);
        try {
            JSONObject queryObj = new JSONObject();
            queryObj.put("sk", searchKey);
            loadTask.execute(queryObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadTask.setCompletionCallback(this);
    }

    @Override
    public void taskCompleted(Object result) {
        this.callback.taskCompleted(result);
    }


}
