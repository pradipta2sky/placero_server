package lm.pkp.com.landmap.drive;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.sync.LMSRestAsyncTask;
import lm.pkp.com.landmap.util.AndroidSystemUtil;

public class DriveDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    private AsyncTaskCallback callback = null;

    public static final String DRIVE_TABLE_NAME = "drive_master";

    public static final String DRIVE_COLUMN_UNIQUE_ID = "unique_id";
    public static final String DRIVE_COLUMN_USER_ID = "user_id";
    public static final String DRIVE_COLUMN_AREA_ID = "area_id";
    public static final String DRIVE_COLUMN_DRIVE_ID = "drive_id";
    public static final String DRIVE_COLUMN_RESOURCE_ID = "resource_id";
    public static final String DRIVE_COLUMN_CONTAINER_DRIVE_ID = "container_id";

    public static final String DRIVE_COLUMN_NAME = "name";
    public static final String DRIVE_COLUMN_TYPE = "type";
    public static final String DRIVE_COLUMN_CONTENT_TYPE = "content_type";
    public static final String DRIVE_COLUMN_MIME_TYPE = "mime_type";
    public static final String DRIVE_COLUMN_SIZE = "size";

    public DriveDBHelper(Context context, AsyncTaskCallback callback) {
        super(context, DATABASE_NAME, null, 1);
        this.callback = callback;
    }

    public DriveDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + DRIVE_TABLE_NAME + "(" +
                        DRIVE_COLUMN_UNIQUE_ID + " text," +
                        DRIVE_COLUMN_AREA_ID + " text," +
                        DRIVE_COLUMN_DRIVE_ID + " text," +
                        DRIVE_COLUMN_USER_ID + " text," +
                        DRIVE_COLUMN_RESOURCE_ID + " text," +
                        DRIVE_COLUMN_CONTAINER_DRIVE_ID + " text," +
                        DRIVE_COLUMN_NAME + " text," +
                        DRIVE_COLUMN_TYPE + " text," +
                        DRIVE_COLUMN_CONTENT_TYPE + " text," +
                        DRIVE_COLUMN_MIME_TYPE + " text," +
                        DRIVE_COLUMN_SIZE + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DRIVE_TABLE_NAME);
        onCreate(db);
    }

    public void insertResourceLocally(DriveResource dr) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DRIVE_COLUMN_UNIQUE_ID, dr.getUniqueId());
        contentValues.put(DRIVE_COLUMN_AREA_ID, dr.getAreaId());
        contentValues.put(DRIVE_COLUMN_DRIVE_ID, dr.getDriveId());
        contentValues.put(DRIVE_COLUMN_USER_ID, dr.getUserId());
        contentValues.put(DRIVE_COLUMN_RESOURCE_ID, dr.getDriveResourceId());
        contentValues.put(DRIVE_COLUMN_CONTAINER_DRIVE_ID, dr.getContainerDriveId());
        contentValues.put(DRIVE_COLUMN_NAME, dr.getName());
        contentValues.put(DRIVE_COLUMN_TYPE, dr.getType());
        contentValues.put(DRIVE_COLUMN_CONTENT_TYPE, dr.getContentType());
        contentValues.put(DRIVE_COLUMN_MIME_TYPE, dr.getMimeType());
        contentValues.put(DRIVE_COLUMN_SIZE, dr.getSize());

        db.insert(DRIVE_TABLE_NAME, null, contentValues);

        db.close();
    }

    public void insertResourceToServer(DriveResource dr){
        LMSRestAsyncTask task = new LMSRestAsyncTask(callback);
        task.execute(preparePostParams("insert", dr));
    }

    public DriveResource insertResourceFromServer(DriveResource dr) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DRIVE_COLUMN_UNIQUE_ID, dr.getUniqueId());
        contentValues.put(DRIVE_COLUMN_AREA_ID, dr.getAreaId());
        contentValues.put(DRIVE_COLUMN_DRIVE_ID, dr.getDriveId());
        contentValues.put(DRIVE_COLUMN_USER_ID, dr.getUserId());
        contentValues.put(DRIVE_COLUMN_RESOURCE_ID, dr.getDriveResourceId());
        contentValues.put(DRIVE_COLUMN_CONTAINER_DRIVE_ID, dr.getContainerDriveId());
        contentValues.put(DRIVE_COLUMN_NAME, dr.getName());
        contentValues.put(DRIVE_COLUMN_TYPE, dr.getType());
        contentValues.put(DRIVE_COLUMN_CONTENT_TYPE, dr.getContentType());
        contentValues.put(DRIVE_COLUMN_MIME_TYPE, dr.getMimeType());
        contentValues.put(DRIVE_COLUMN_SIZE, dr.getSize());

        db.insert(DRIVE_TABLE_NAME, null, contentValues);
        db.close();
        return dr;
    }

    public boolean updateResource(DriveResource dr) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DRIVE_COLUMN_UNIQUE_ID, dr.getUniqueId());
        contentValues.put(DRIVE_COLUMN_AREA_ID, dr.getAreaId());
        contentValues.put(DRIVE_COLUMN_DRIVE_ID, dr.getDriveId());
        contentValues.put(DRIVE_COLUMN_USER_ID, dr.getUserId());
        contentValues.put(DRIVE_COLUMN_RESOURCE_ID, dr.getDriveResourceId());
        contentValues.put(DRIVE_COLUMN_CONTAINER_DRIVE_ID, dr.getContainerDriveId());
        contentValues.put(DRIVE_COLUMN_NAME, dr.getName());
        contentValues.put(DRIVE_COLUMN_TYPE, dr.getType());
        contentValues.put(DRIVE_COLUMN_CONTENT_TYPE, dr.getContentType());
        contentValues.put(DRIVE_COLUMN_MIME_TYPE, dr.getMimeType());
        contentValues.put(DRIVE_COLUMN_SIZE, dr.getSize());

        db.update(DRIVE_TABLE_NAME, contentValues, DRIVE_COLUMN_UNIQUE_ID + " = ? ", new String[]{dr.getUniqueId()});
        new LMSRestAsyncTask().execute(preparePostParams("update", dr));
        db.close();

        return true;
    }

    public void deleteResource(DriveResource dr) {
        if(dr == null){
            return;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DRIVE_TABLE_NAME,
                DRIVE_COLUMN_UNIQUE_ID + " = ? ",
                new String[]{dr.getUniqueId()});

        JSONObject areaPostParams = preparePostParams("delete", dr);
        new LMSRestAsyncTask().execute(areaPostParams);

        db.close();
    }

    public ArrayList<DriveResource> getAllResources() {
        ArrayList<DriveResource> allResources = new ArrayList<DriveResource>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + DRIVE_TABLE_NAME, null);
            if (cursor == null) {
                return allResources;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    DriveResource dr = new DriveResource();

                    dr.setUniqueId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_UNIQUE_ID)));
                    dr.setAreaId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_AREA_ID)));
                    dr.setDriveId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_DRIVE_ID)));
                    dr.setUserId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_USER_ID)));
                    dr.setContainerDriveId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTAINER_DRIVE_ID)));
                    dr.setDriveResourceId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_RESOURCE_ID)));
                    dr.setName(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_NAME)));
                    dr.setType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_TYPE)));
                    dr.setContentType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTENT_TYPE)));
                    dr.setMimeType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_MIME_TYPE)));
                    dr.setSize(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_SIZE)));

                    allResources.add(dr);
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return allResources;
    }

    public DriveResource getResourceById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        DriveResource dr = null;
        try {
            cursor = db.rawQuery("select * from " + DRIVE_TABLE_NAME + " WHERE " + DRIVE_COLUMN_UNIQUE_ID + "=?",
                    new String[]{id});
            if (cursor == null) {
                return null;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    dr = new DriveResource();

                    dr.setUniqueId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_UNIQUE_ID)));
                    dr.setAreaId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_AREA_ID)));
                    dr.setDriveId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_DRIVE_ID)));
                    dr.setUserId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_USER_ID)));
                    dr.setContainerDriveId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTAINER_DRIVE_ID)));
                    dr.setDriveResourceId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_RESOURCE_ID)));
                    dr.setName(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_NAME)));
                    dr.setType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_TYPE)));
                    dr.setContentType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTENT_TYPE)));
                    dr.setMimeType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_MIME_TYPE)));
                    dr.setSize(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_SIZE)));

                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return dr;
    }

    public ArrayList<DriveResource> getFolderResourcesByUid(String uid) {
        ArrayList<DriveResource> allResources = new ArrayList<DriveResource>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + DRIVE_TABLE_NAME + " WHERE "
                            + DRIVE_COLUMN_USER_ID + "= ? AND " + DRIVE_COLUMN_TYPE + "= 'folder'",
                    new String[]{uid});
            if (cursor == null) {
                return allResources;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    DriveResource dr = new DriveResource();

                    dr.setUniqueId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_UNIQUE_ID)));
                    dr.setAreaId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_AREA_ID)));
                    dr.setDriveId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_DRIVE_ID)));
                    dr.setUserId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_USER_ID)));
                    dr.setContainerDriveId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTAINER_DRIVE_ID)));
                    dr.setDriveResourceId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_RESOURCE_ID)));
                    dr.setName(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_NAME)));
                    dr.setType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_TYPE)));
                    dr.setContentType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTENT_TYPE)));
                    dr.setMimeType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_MIME_TYPE)));
                    dr.setSize(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_SIZE)));

                    allResources.add(dr);
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return allResources;
    }

    public ArrayList<DriveResource> getDriveResourcesByAreaId(String aid, String type) {
        ArrayList<DriveResource> allResources = new ArrayList<DriveResource>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + DRIVE_TABLE_NAME + " WHERE "
                            + DRIVE_COLUMN_AREA_ID + "=? and " + DRIVE_COLUMN_TYPE + "='" + type + "'",
                    new String[]{aid});
            if (cursor == null) {
                return allResources;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    DriveResource dr = new DriveResource();

                    dr.setUniqueId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_UNIQUE_ID)));
                    dr.setAreaId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_AREA_ID)));
                    dr.setDriveId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_DRIVE_ID)));
                    dr.setUserId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_USER_ID)));
                    dr.setContainerDriveId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTAINER_DRIVE_ID)));
                    dr.setDriveResourceId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_RESOURCE_ID)));
                    dr.setName(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_NAME)));
                    dr.setType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_TYPE)));
                    dr.setContentType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTENT_TYPE)));
                    dr.setMimeType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_MIME_TYPE)));
                    dr.setSize(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_SIZE)));

                    allResources.add(dr);
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return allResources;
    }

    public ArrayList<DriveResource> getDriveResourcesByAreaId(String aid) {
        ArrayList<DriveResource> allResources = new ArrayList<DriveResource>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + DRIVE_TABLE_NAME + " WHERE "
                            + DRIVE_COLUMN_AREA_ID + "=?",
                    new String[]{aid});
            if (cursor == null) {
                return allResources;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    DriveResource dr = new DriveResource();

                    dr.setUniqueId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_UNIQUE_ID)));
                    dr.setAreaId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_AREA_ID)));
                    dr.setDriveId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_DRIVE_ID)));
                    dr.setUserId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_USER_ID)));
                    dr.setContainerDriveId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTAINER_DRIVE_ID)));
                    dr.setDriveResourceId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_RESOURCE_ID)));
                    dr.setName(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_NAME)));
                    dr.setType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_TYPE)));
                    dr.setContentType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTENT_TYPE)));
                    dr.setMimeType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_MIME_TYPE)));
                    dr.setSize(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_SIZE)));

                    allResources.add(dr);
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return allResources;
    }

    private JSONObject preparePostParams(String queryType, DriveResource dr) {
        JSONObject postParams = new JSONObject();
        try {
            postParams.put("requestType", "DriveMaster");
            postParams.put("query_type", queryType);
            postParams.put("device_id", AndroidSystemUtil.getDeviceId());
            postParams.put(DRIVE_COLUMN_AREA_ID, dr.getAreaId());
            postParams.put(DRIVE_COLUMN_USER_ID, dr.getUserId());
            postParams.put(DRIVE_COLUMN_UNIQUE_ID, dr.getUniqueId());
            postParams.put(DRIVE_COLUMN_DRIVE_ID, dr.getDriveId());
            postParams.put(DRIVE_COLUMN_CONTAINER_DRIVE_ID, dr.getContainerDriveId());
            postParams.put(DRIVE_COLUMN_RESOURCE_ID, dr.getDriveResourceId());
            postParams.put(DRIVE_COLUMN_NAME, dr.getName());
            postParams.put(DRIVE_COLUMN_TYPE, dr.getType());
            postParams.put(DRIVE_COLUMN_CONTENT_TYPE, dr.getContentType());
            postParams.put(DRIVE_COLUMN_MIME_TYPE, dr.getMimeType());
            postParams.put(DRIVE_COLUMN_SIZE, dr.getSize());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
    }

    public void deleteResourcesByAreaId(String areaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + DRIVE_TABLE_NAME + " WHERE "
                + DRIVE_COLUMN_AREA_ID + " = '" + areaId + "'");
        db.close();
    }

    public void deleteDriveElementsLocally() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DRIVE_TABLE_NAME, "1", null);
        db.close();
    }

    public void setCompletionCallback(AsyncTaskCallback callback) {
        this.callback = callback;
    }

    public void finalizeTaskCompletion(){
        callback.taskCompleted("");
    }
}