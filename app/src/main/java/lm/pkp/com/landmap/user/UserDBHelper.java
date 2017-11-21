package lm.pkp.com.landmap.user;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.permission.PermissionsDBHelper;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.sync.LMSRestAsyncTask;
import lm.pkp.com.landmap.util.AndroidSystemUtil;
import lm.pkp.com.landmap.weather.db.WeatherDBHelper;

public class UserDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    private Context localContext;

    public static final String USER_TABLE_NAME = "user_master";
    public static final String USER_COLUMN_DISPLAY_NAME = "display_name";
    public static final String USER_COLUMN_EMAIL = "email";
    public static final String USER_COLUMN_FAMILY_NAME = "family_name";
    public static final String USER_COLUMN_GIVEN_NAME = "given_name";
    public static final String USER_COLUMN_PHOTO_URL = "photo_url";
    public static final String USER_COLUMN_AUTH_SYS_ID = "auth_sys_id";

    public UserDBHelper(Context context) {
        super(context, UserDBHelper.DATABASE_NAME, null, 1);
        this.localContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + UserDBHelper.USER_TABLE_NAME + "(" +
                        UserDBHelper.USER_COLUMN_DISPLAY_NAME + " text, " +
                        UserDBHelper.USER_COLUMN_EMAIL + " text," +
                        UserDBHelper.USER_COLUMN_FAMILY_NAME + " text," +
                        UserDBHelper.USER_COLUMN_GIVEN_NAME + " text, " +
                        UserDBHelper.USER_COLUMN_PHOTO_URL + " text, " +
                        UserDBHelper.USER_COLUMN_AUTH_SYS_ID + " text )"
        );

        new AreaDBHelper(this.localContext).onCreate(db);
        new PositionsDBHelper(this.localContext).onCreate(db);
        new DriveDBHelper(this.localContext).onCreate(db);
        new PermissionsDBHelper(this.localContext).onCreate(db);
        new WeatherDBHelper(this.localContext).onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + UserDBHelper.USER_TABLE_NAME);
        this.onCreate(db);
    }

    public void insertUserLocally(UserElement user) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(UserDBHelper.USER_COLUMN_DISPLAY_NAME, user.getDisplayName());
        contentValues.put(UserDBHelper.USER_COLUMN_EMAIL, user.getEmail());
        contentValues.put(UserDBHelper.USER_COLUMN_FAMILY_NAME, user.getFamilyName());
        contentValues.put(UserDBHelper.USER_COLUMN_GIVEN_NAME, user.getGivenName());
        contentValues.put(UserDBHelper.USER_COLUMN_PHOTO_URL, user.getPhotoUrl());
        contentValues.put(UserDBHelper.USER_COLUMN_AUTH_SYS_ID, user.getAuthSystemId());

        db.insert(UserDBHelper.USER_TABLE_NAME, null, contentValues);
        db.close();
    }

    public void insertUserToServer(UserElement user) {
        JSONObject postParams = this.preparePostParams("insert", user);
        new LMSRestAsyncTask().execute(postParams);
    }

    private JSONObject preparePostParams(String queryType, UserElement user) {
        JSONObject postParams = new JSONObject();
        try {
            postParams.put("queryType", queryType);
            postParams.put("deviceID", AndroidSystemUtil.getDeviceId());
            postParams.put("requestType", "UserMaster");
            postParams.put(UserDBHelper.USER_COLUMN_DISPLAY_NAME, user.getDisplayName());
            postParams.put(UserDBHelper.USER_COLUMN_FAMILY_NAME, user.getFamilyName());
            postParams.put(UserDBHelper.USER_COLUMN_GIVEN_NAME, user.getGivenName());
            postParams.put(UserDBHelper.USER_COLUMN_AUTH_SYS_ID, user.getAuthSystemId());
            postParams.put(UserDBHelper.USER_COLUMN_EMAIL, user.getEmail());
            postParams.put(UserDBHelper.USER_COLUMN_PHOTO_URL, user.getPhotoUrl());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
    }

    public UserElement getUserByEmail(String email) {
        Cursor cursor = null;
        UserElement ue = null;
        SQLiteDatabase db = getReadableDatabase();
        try {
            cursor = db.rawQuery("SELECT * FROM " + UserDBHelper.USER_TABLE_NAME + " WHERE " + UserDBHelper.USER_COLUMN_EMAIL + "=?",
                    new String[]{email});
            if (cursor == null) {
                return ue;
            }
            if (cursor.getCount() > 0) {
                ue = new UserElement();
                cursor.moveToFirst();
                ue.setDisplayName(cursor.getString(cursor.getColumnIndex(UserDBHelper.USER_COLUMN_DISPLAY_NAME)));
                ue.setPhotoUrl(cursor.getString(cursor.getColumnIndex(UserDBHelper.USER_COLUMN_PHOTO_URL)));
                ue.setGivenName(cursor.getString(cursor.getColumnIndex(UserDBHelper.USER_COLUMN_GIVEN_NAME)));
                ue.setFamilyName(cursor.getString(cursor.getColumnIndex(UserDBHelper.USER_COLUMN_FAMILY_NAME)));
                ue.setAuthSystemId(cursor.getString(cursor.getColumnIndex(UserDBHelper.USER_COLUMN_AUTH_SYS_ID)));
                ue.setEmail(email);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return ue;
    }

}