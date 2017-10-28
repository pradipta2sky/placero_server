package lm.pkp.com.landmap.user;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import lm.pkp.com.landmap.area.AreaDBHelper;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.sync.LandMapAsyncRestSync;
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

        JSONObject postParams = preparePostParams("insert", contentValues, null ,null);
        new LandMapAsyncRestSync().execute(postParams);

        db.close();
        return true;
    }

    private JSONObject preparePostParams(String queryType, ContentValues contentValues,String id ,String deviceID) {
        JSONObject postParams = new JSONObject();
        try {
            if (id != null) {
                postParams.put("id", id);
            }
            if (deviceID == null) {
                deviceID = AndroidSystemUtil.getDeviceId();
            }
            postParams.put("queryType", queryType);
            postParams.put("deviceID", deviceID);
            postParams.put("requestType", "UserMaster");
            postParams.put(USER_COLUMN_DISPLAY_NAME, contentValues.get(USER_COLUMN_DISPLAY_NAME));
            if(queryType=="search"){
                postParams.put("requestType", "UserSearch");
            }else {
                postParams.put(USER_COLUMN_FAMILY_NAME, contentValues.get(USER_COLUMN_FAMILY_NAME));
                postParams.put(USER_COLUMN_GIVEN_NAME, contentValues.get(USER_COLUMN_GIVEN_NAME));
                postParams.put(USER_COLUMN_AUTH_SYS_ID, contentValues.get(USER_COLUMN_AUTH_SYS_ID));
                postParams.put(USER_COLUMN_EMAIL, contentValues.get(USER_COLUMN_EMAIL));
                postParams.put(USER_COLUMN_PHOTO_URL, contentValues.get(USER_COLUMN_PHOTO_URL));
            }
            } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
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