package lm.pkp.com.landmap.sync;

import android.content.Context;

import org.json.JSONObject;

import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.tasks.PublicAreasLoadTask;
import lm.pkp.com.landmap.area.tasks.UserAreaDetailsLoadTask;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.permission.PermissionsDBHelper;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.tags.TagsDBHelper;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.weather.db.WeatherDBHelper;

/**
 * Created by USER on 11/4/2017.
 */
public class LocalDataRefresher implements AsyncTaskCallback {

    private Context context;
    private AsyncTaskCallback callback;

    public LocalDataRefresher(Context context, AsyncTaskCallback caller) {
        this.context = context;
        this.callback = caller;
    }

    public void refreshLocalData() {

        AreaDBHelper adh = new AreaDBHelper(this.context);
        adh.deleteAreasLocally();

        PositionsDBHelper pdh = new PositionsDBHelper(this.context);
        pdh.deletePositionsLocally();

        WeatherDBHelper wdh = new WeatherDBHelper(this.context);
        wdh.deleteWeatherElementsLocally();

        DriveDBHelper ddh = new DriveDBHelper(this.context);
        ddh.cleanLocalDriveResources();

        PermissionsDBHelper pmh = new PermissionsDBHelper(this.context);
        pmh.deletePermissionsLocally();

        TagsDBHelper tdh = new TagsDBHelper(this.context);
        tdh.deleteAllTagsLocally();

        UserAreaDetailsLoadTask loadTask = new UserAreaDetailsLoadTask(this.context);
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
        AreaDBHelper adh = new AreaDBHelper(this.context);
        adh.deletePublicAreas();

        PublicAreasLoadTask loadTask = new PublicAreasLoadTask(this.context);
        loadTask.setCompletionCallback(this);
        try {
            loadTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshPublicAreas(String searchKey) {
        AreaDBHelper adh = new AreaDBHelper(this.context);
        adh.deletePublicAreas();

        PublicAreasLoadTask loadTask = new PublicAreasLoadTask(this.context);
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
        if(callback != null){
            this.callback.taskCompleted(result);
        }
    }

}
