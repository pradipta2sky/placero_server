package lm.pkp.com.landmap.permission;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.sync.LMSRestAsyncTask;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserElement;

public class PermissionsDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    private AsyncTaskCallback callback = null;

    public static final String ACCESS_TABLE_NAME = "area_access";
    public static final String ACCESS_COLUMN_AREA_ID = "area_id";
    public static final String ACCESS_COLUMN_USER_ID = "user_id";
    public static final String ACCESS_COLUMN_FUNCTION_CODE = "function_code";

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
                        ACCESS_COLUMN_AREA_ID + " text," +
                        ACCESS_COLUMN_USER_ID + " text, " +
                        ACCESS_COLUMN_FUNCTION_CODE + " text)"
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
        contentValues.put(ACCESS_COLUMN_AREA_ID, pe.getAreaId());
        contentValues.put(ACCESS_COLUMN_FUNCTION_CODE, pe.getFunctionCode());
        contentValues.put(ACCESS_COLUMN_USER_ID, pe.getUserId());

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
        final UserElement userElement = UserContext.getInstance().getUserElement();
        try {
            postParams.put("requestType", "AreaShare");
            postParams.put("query_type", queryType);
            postParams.put("source_user", userElement.getEmail());
            postParams.put("target_user", targetUser);
            postParams.put("area_id", areaElement.getUniqueId());
            postParams.put("function_codes", functionCodes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
    }

    public void deletePermissionsByAreaId(String areaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + ACCESS_TABLE_NAME + " WHERE "
                + ACCESS_COLUMN_AREA_ID + " = '" + areaId + "'");
        db.close();
    }

    public Map<String, PermissionElement> fetchPermissionsByAreaId(String areaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Map<String, PermissionElement> perMap = new HashMap<>();
        Cursor cursor = db.rawQuery("select * from " + ACCESS_TABLE_NAME
                + " WHERE " + ACCESS_COLUMN_AREA_ID + "=?", new String[]{areaId});
        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                PermissionElement pe = new PermissionElement();
                pe.setUserId(cursor.getString(cursor.getColumnIndex(ACCESS_COLUMN_USER_ID)));
                pe.setAreaId(areaId);
                final String functionCode = cursor.getString(cursor.getColumnIndex(ACCESS_COLUMN_FUNCTION_CODE));
                pe.setFunctionCode(functionCode);
                perMap.put(functionCode, pe);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return perMap;
    }

    public void setCompletionCallback(AsyncTaskCallback callback) {
        this.callback = callback;
    }

    public void finalizeTaskCompletion() {
        callback.taskCompleted("");
    }

}