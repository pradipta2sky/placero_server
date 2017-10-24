package lm.pkp.com.landmap.user;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import lm.pkp.com.landmap.AreaDBHelper;
import lm.pkp.com.landmap.AreaElement;
import lm.pkp.com.landmap.LandMapAsyncRestSync;
import lm.pkp.com.landmap.PositionsDBHelper;
import lm.pkp.com.landmap.util.AndroidSystemUtil;

public class UserDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    private Context localContext = null;

    public static final String USER_TABLE_NAME = "user_master";
    public static final String USER_COLUMN_DISPLAY_NAME = "display_name";
    public static final String USER_COLUMN_EMAIL = "email";
    public static final String USER_COLUMN_FAMILY_NAME = "family_name";
    public static final String USER_COLUMN_GIVEN_NAME = "given_name";
    public static final String USER_COLUMN_PHOTO_URL = "photo_url";
    public static final String USER_COLUMN_AUTH_SYS_ID = "auth_sys_id";

    public UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        localContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + USER_TABLE_NAME + "(" +
                        USER_COLUMN_DISPLAY_NAME + " text, " +
                        USER_COLUMN_EMAIL + " text," +
                        USER_COLUMN_FAMILY_NAME + " text," +
                        USER_COLUMN_GIVEN_NAME + " text, " +
                        USER_COLUMN_PHOTO_URL + " text, " +
                        USER_COLUMN_AUTH_SYS_ID + " text )"
        );
        new AreaDBHelper(localContext).onCreate(db);
        new PositionsDBHelper(localContext).onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertUser(UserElement user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USER_COLUMN_DISPLAY_NAME, user.getDisplayName());
        contentValues.put(USER_COLUMN_EMAIL, user.getEmail());
        contentValues.put(USER_COLUMN_FAMILY_NAME, user.getFamilyName());
        contentValues.put(USER_COLUMN_GIVEN_NAME, user.getGivenName());
        contentValues.put(USER_COLUMN_PHOTO_URL, user.getPhotoUrl());
        contentValues.put(USER_COLUMN_AUTH_SYS_ID, user.getAuthSystemId());
        db.insert(USER_TABLE_NAME, null, contentValues);

        //JSONObject postParams = preparePostParams("insert", null, centerLon, centerLat, desc, name, null, uniqueId);
        //new LandMapAsyncRestSync().execute(postParams);

        return true;
    }

    /*
    private JSONObject preparePostParams(String queryType, String unique_id) {
        JSONObject postParams = new JSONObject();
        postParams = preparePostParams(queryType, null, null, null, null, null, AndroidSystemUtil.getDeviceId(), unique_id);
        new LandMapAsyncRestSync().execute(postParams);
        return postParams;
    }

    private JSONObject preparePostParams(String queryType, Integer id, String centerlon, String centerlat, String desc, String name,
                                         String deviceID, String unique_id) {
        JSONObject postParams = new JSONObject();
        try {
            if (id != null) {
                postParams.put("id", id);
            }
            if (deviceID == null) {
                deviceID = AndroidSystemUtil.getDeviceId();
            }
            postParams.put("requestType", "AreaMaster");
            postParams.put("queryType", queryType);
            postParams.put("deviceID", deviceID);
            postParams.put("center_lon", centerlon);
            postParams.put("center_lat", centerlat);
            postParams.put("desc", desc);
            postParams.put("name", name);
            postParams.put("unique_id", unique_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
    }
    */

    public Cursor getData(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + USER_TABLE_NAME + " where " + USER_COLUMN_EMAIL + "='" + email + "'", null);
        return res;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, USER_TABLE_NAME);
        return numRows;
    }

    public UserElement getUserByEmail(String email) {
        Cursor cursor = null;
        UserElement ue = null;
        try {
            cursor = this.getReadableDatabase().rawQuery("SELECT * FROM " + USER_TABLE_NAME + " WHERE " + USER_COLUMN_EMAIL + "=?",
                    new String[]{email});
            if (cursor == null) {
                return ue;
            }
            if (cursor.getCount() > 0) {
                ue = new UserElement();
                cursor.moveToFirst();
                ue.setDisplayName(cursor.getString(cursor.getColumnIndex(USER_COLUMN_DISPLAY_NAME)));
                ue.setPhotoUrl(cursor.getString(cursor.getColumnIndex(USER_COLUMN_PHOTO_URL)));
                ue.setGivenName(cursor.getString(cursor.getColumnIndex(USER_COLUMN_GIVEN_NAME)));
                ue.setFamilyName(cursor.getString(cursor.getColumnIndex(USER_COLUMN_FAMILY_NAME)));
                ue.setAuthSystemId(cursor.getString(cursor.getColumnIndex(USER_COLUMN_AUTH_SYS_ID)));
                ue.setEmail(email);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ue;
    }

}