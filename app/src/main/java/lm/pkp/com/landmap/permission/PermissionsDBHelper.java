package lm.pkp.com.landmap.permission;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.api.services.drive.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.sync.LMSRestAsyncTask;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserElement;
import lm.pkp.com.landmap.util.AndroidSystemUtil;

public class PermissionsDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    private AsyncTaskCallback callback = null;

    public static final String ACCESS_TABLE_NAME = "area_access";
    public static final String ACCESS_AREA_ID = "area_id";
    public static final String ACCESS_USER_ID = "user_id";
    public static final String ACCESS_FUNCTION_CODE = "function_code";

    public PermissionsDBHelper(Context context, AsyncTaskCallback callback) {
        super(context, DATABASE_NAME, null, 1);
        this.callback = callback;
    }

    public PermissionsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + ACCESS_TABLE_NAME + "(" +
                        ACCESS_AREA_ID + " text," +
                        ACCESS_USER_ID + " text, " +
                        ACCESS_FUNCTION_CODE + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ACCESS_TABLE_NAME);
        onCreate(db);
    }

    public PermissionElement insertPermission(PermissionElement pe) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(ACCESS_AREA_ID, pe.getAreaId());
        contentValues.put(ACCESS_FUNCTION_CODE, pe.getFunctionCode());
        contentValues.put(ACCESS_USER_ID, pe.getUserId());

        db.insert(ACCESS_TABLE_NAME, null, contentValues);
        db.close();
        return pe;
    }

    public void insertPermissionsToServer(String targetUser, String functionCodes) {
        LMSRestAsyncTask task = new LMSRestAsyncTask(callback);
        task.execute(preparePostParams("insert", targetUser, functionCodes));
    }

    private JSONObject preparePostParams(String queryType, String targetUser, String functionCodes) {
        JSONObject postParams = new JSONObject();
        final AreaElement areaElement = AreaContext.getInstance().getAreaElement();
        try {
            postParams.put("requestType", "ShareArea");
            postParams.put("query_type", queryType);
            postParams.put("user_id", targetUser);
            postParams.put("area_id", areaElement.getUniqueId());
            postParams.put("function_codes",functionCodes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  postParams;
    }


    public void setCompletionCallback(AsyncTaskCallback callback) {
        this.callback = callback;
    }

    public void finalizeTaskCompletion(){
        callback.taskCompleted("");
    }

}