package lm.pkp.com.landmap.tags;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.permission.PermissionElement;
import lm.pkp.com.landmap.sync.LMSRestAsyncTask;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserElement;

public class TagsDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    public static final String ACCESS_TABLE_NAME = "tag_master";
    public static final String ACCESS_COLUMN_NAME = "name";
    public static final String ACCESS_COLUMN_CONTEXT = "context";
    public static final String ACCESS_COLUMN_CONTEXT_ID = "context_id";
    private AsyncTaskCallback callback;

    public TagsDBHelper(Context context, AsyncTaskCallback callback) {
        super(context, DATABASE_NAME, null, 1);
        this.callback = callback;
    }

    public TagsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " +
                        ACCESS_TABLE_NAME + "(" +
                        ACCESS_COLUMN_NAME + " text," +
                        ACCESS_COLUMN_CONTEXT + " text, " +
                        ACCESS_COLUMN_CONTEXT_ID + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ACCESS_TABLE_NAME);
        this.onCreate(db);
    }

    public void insertTagsLocally(List<String> names, String context, String contextId) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        if(context.equalsIgnoreCase("user")){
            contentValues.put(ACCESS_COLUMN_CONTEXT_ID, contextId);
            contentValues.put(ACCESS_COLUMN_CONTEXT, "user");
        }else {
            contentValues.put(ACCESS_COLUMN_CONTEXT_ID, contextId);
            contentValues.put(ACCESS_COLUMN_CONTEXT, "area");
        }
        for(String name : names){
            contentValues.put(ACCESS_COLUMN_NAME, name);
            db.insert(ACCESS_TABLE_NAME, null, contentValues);
        }
        db.close();
    }

    public void insertTagsToServer(List<String> names, String context) {
        TagInsertAsyncTask task = new TagInsertAsyncTask(callback);
        task.execute(preparePostParams(names, context));
    }

    private JSONObject preparePostParams(List<String> names, String context) {
        JSONObject postParams = new JSONObject();
        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        UserElement userElement = UserContext.getInstance().getUserElement();
        try {
            if(names.size() == 1){
                postParams.put("query_type", "insert_single");
                postParams.put("name", names.get(0));
            }else {
                postParams.put("query_type", "insert_multiple");
                Object[] nameArr = names.toArray();
                String joinedNames = StringUtils.join(nameArr, ",");
                postParams.put("names", joinedNames);
            }
            postParams.put("context", context);
            if(context.equalsIgnoreCase("user")){
                postParams.put("context_id", userElement.getEmail());
            }else {
                postParams.put("context_id", areaElement.getUniqueId());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
    }

    public void deleteTagsByContext(String context, String contextId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(ACCESS_TABLE_NAME, ACCESS_COLUMN_CONTEXT + "=? AND " + ACCESS_COLUMN_CONTEXT_ID + "=?",
                new String[]{context, contextId});
        db.close();
    }

    public void deleteAllTagsLocally() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(ACCESS_TABLE_NAME, "1", null);
        db.close();
    }

    public void setCompletionCallback(AsyncTaskCallback callback) {
        this.callback = callback;
    }

    public void finalizeTaskCompletion() {
        this.callback.taskCompleted("");
    }

}