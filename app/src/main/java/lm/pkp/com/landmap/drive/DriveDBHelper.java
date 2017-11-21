package lm.pkp.com.landmap.drive;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.sync.LMSRestAsyncTask;
import lm.pkp.com.landmap.util.AndroidSystemUtil;

public class DriveDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    private AsyncTaskCallback callback;

    public static final String DRIVE_TABLE_NAME = "drive_master";

    public static final String DRIVE_COLUMN_UNIQUE_ID = "unique_id";
    public static final String DRIVE_COLUMN_USER_ID = "user_id";
    public static final String DRIVE_COLUMN_AREA_ID = "area_id";
    public static final String DRIVE_COLUMN_RESOURCE_ID = "resource_id";
    public static final String DRIVE_COLUMN_CONTAINER_ID = "container_id";

    public static final String DRIVE_COLUMN_NAME = "name";
    public static final String DRIVE_COLUMN_TYPE = "type";
    public static final String DRIVE_COLUMN_CONTENT_TYPE = "content_type";
    public static final String DRIVE_COLUMN_MIME_TYPE = "mime_type";
    public static final String DRIVE_COLUMN_SIZE = "size";
    public static final String DRIVE_COLUMN_LATITUDE = "latitude";
    public static final String DRIVE_COLUMN_LONGITUDE = "longitude";
    public static final String DRIVE_COLUMN_CREATED_MILLIS = "created_millis";

    public DriveDBHelper(Context context, AsyncTaskCallback callback) {
        super(context, DriveDBHelper.DATABASE_NAME, null, 1);
        this.callback = callback;
    }

    public DriveDBHelper(Context context) {
        super(context, DriveDBHelper.DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + DriveDBHelper.DRIVE_TABLE_NAME + "(" +
                        DriveDBHelper.DRIVE_COLUMN_UNIQUE_ID + " text," +
                        DriveDBHelper.DRIVE_COLUMN_AREA_ID + " text," +
                        DriveDBHelper.DRIVE_COLUMN_USER_ID + " text," +
                        DriveDBHelper.DRIVE_COLUMN_RESOURCE_ID + " text," +
                        DriveDBHelper.DRIVE_COLUMN_CONTAINER_ID + " text," +
                        DriveDBHelper.DRIVE_COLUMN_NAME + " text," +
                        DriveDBHelper.DRIVE_COLUMN_TYPE + " text," +
                        DriveDBHelper.DRIVE_COLUMN_CONTENT_TYPE + " text," +
                        DriveDBHelper.DRIVE_COLUMN_MIME_TYPE + " text," +
                        DriveDBHelper.DRIVE_COLUMN_LATITUDE + " text," +
                        DriveDBHelper.DRIVE_COLUMN_LONGITUDE + " text," +
                        DriveDBHelper.DRIVE_COLUMN_CREATED_MILLIS + " text," +
                        DriveDBHelper.DRIVE_COLUMN_SIZE + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DriveDBHelper.DRIVE_TABLE_NAME);
        this.onCreate(db);
    }

    public void insertResourceLocally(DriveResource dr) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_UNIQUE_ID, dr.getUniqueId());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_AREA_ID, dr.getAreaId());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_USER_ID, dr.getUserId());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_RESOURCE_ID, dr.getResourceId());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_CONTAINER_ID, dr.getContainerId());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_NAME, dr.getName());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_TYPE, dr.getType());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_CONTENT_TYPE, dr.getContentType());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_MIME_TYPE, dr.getMimeType());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_SIZE, dr.getSize());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_LATITUDE, dr.getLatitude());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_LONGITUDE, dr.getLongitude());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_CREATED_MILLIS, dr.getCreatedOnMillis());

        db.insert(DriveDBHelper.DRIVE_TABLE_NAME, null, contentValues);
        db.close();
    }

    public void insertResourceToServer(DriveResource dr) {
        LMSRestAsyncTask task = new LMSRestAsyncTask(this.callback);
        task.execute(this.preparePostParams("insert", dr));
    }

    public void updateResourceOnServer(DriveResource dr) {
        LMSRestAsyncTask task = new LMSRestAsyncTask(this.callback);
        task.execute(this.preparePostParams("update", dr));
    }

    public DriveResource insertResourceFromServer(DriveResource dr) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_UNIQUE_ID, dr.getUniqueId());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_AREA_ID, dr.getAreaId());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_USER_ID, dr.getUserId());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_RESOURCE_ID, dr.getResourceId());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_CONTAINER_ID, dr.getContainerId());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_NAME, dr.getName());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_TYPE, dr.getType());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_CONTENT_TYPE, dr.getContentType());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_MIME_TYPE, dr.getMimeType());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_SIZE, dr.getSize());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_LATITUDE, dr.getLatitude());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_LONGITUDE, dr.getLongitude());
        contentValues.put(DriveDBHelper.DRIVE_COLUMN_CREATED_MILLIS, dr.getCreatedOnMillis());

        db.insert(DriveDBHelper.DRIVE_TABLE_NAME, null, contentValues);
        db.close();
        return dr;
    }

    public ArrayList<DriveResource> getDriveResourcesByAreaId(String aid) {
        ArrayList<DriveResource> allResources = new ArrayList<DriveResource>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + DriveDBHelper.DRIVE_TABLE_NAME + " WHERE "
                            + DriveDBHelper.DRIVE_COLUMN_AREA_ID + "=?",
                    new String[]{aid});
            if (cursor == null) {
                return allResources;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    DriveResource dr = new DriveResource();

                    dr.setUniqueId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_UNIQUE_ID)));
                    dr.setAreaId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_AREA_ID)));
                    dr.setUserId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_USER_ID)));
                    dr.setContainerId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_CONTAINER_ID)));
                    dr.setResourceId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_RESOURCE_ID)));
                    dr.setName(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_NAME)));
                    dr.setType(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_TYPE)));
                    dr.setContentType(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_CONTENT_TYPE)));
                    dr.setMimeType(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_MIME_TYPE)));
                    dr.setSize(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_SIZE)));
                    dr.setLatitude(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_LATITUDE)));
                    dr.setLongitude(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_LONGITUDE)));
                    dr.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_CREATED_MILLIS)));

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

    public DriveResource getDriveResourcesByResourceId(String resourceID) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        DriveResource resource = new DriveResource();
        try {
            cursor = db.rawQuery("select * from " + DriveDBHelper.DRIVE_TABLE_NAME + " WHERE "
                            + DriveDBHelper.DRIVE_COLUMN_RESOURCE_ID + "=?",
                    new String[]{resourceID});
            if (cursor == null) {
                return null;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    resource.setUniqueId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_UNIQUE_ID)));
                    resource.setAreaId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_AREA_ID)));
                    resource.setUserId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_USER_ID)));
                    resource.setContainerId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_CONTAINER_ID)));
                    resource.setResourceId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_RESOURCE_ID)));
                    resource.setName(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_NAME)));
                    resource.setType(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_TYPE)));
                    resource.setContentType(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_CONTENT_TYPE)));
                    resource.setMimeType(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_MIME_TYPE)));
                    resource.setSize(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_SIZE)));
                    resource.setLatitude(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_LATITUDE)));
                    resource.setLongitude(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_LONGITUDE)));
                    resource.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_CREATED_MILLIS)));
                    break;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return resource;
    }

    public DriveResource getDriveResourceRoot(String parentName) {
        DriveResource childResource = new DriveResource();
        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + DriveDBHelper.DRIVE_TABLE_NAME + " WHERE "
                            + DriveDBHelper.DRIVE_COLUMN_AREA_ID + "=? AND " + DriveDBHelper.DRIVE_COLUMN_TYPE + "='folder' AND "
                            + DriveDBHelper.DRIVE_COLUMN_CONTENT_TYPE + "='folder'",
                    new String[]{areaElement.getUniqueId()});
            if (cursor == null) {
                return null;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    DriveResource resource = new DriveResource();
                    resource.setUniqueId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_UNIQUE_ID)));
                    resource.setAreaId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_AREA_ID)));
                    resource.setUserId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_USER_ID)));
                    resource.setContainerId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_CONTAINER_ID)));
                    resource.setResourceId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_RESOURCE_ID)));
                    resource.setName(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_NAME)));
                    resource.setType(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_TYPE)));
                    resource.setContentType(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_CONTENT_TYPE)));
                    resource.setMimeType(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_MIME_TYPE)));
                    resource.setSize(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_SIZE)));
                    resource.setLatitude(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_LATITUDE)));
                    resource.setLongitude(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_LONGITUDE)));
                    resource.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_CREATED_MILLIS)));

                    Map<String, DriveResource> commonResources = this.getCommonResources();
                    DriveResource commonParent = commonResources.get(parentName);
                    if (resource.getContainerId().equals(commonParent.getResourceId())) {
                        childResource = resource;
                        break;
                    } else {
                        cursor.moveToNext();
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return childResource;
    }

    public Map<String, DriveResource> getCommonResources() {
        Map<String, DriveResource> resources = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + DriveDBHelper.DRIVE_TABLE_NAME + " WHERE "
                            + DriveDBHelper.DRIVE_COLUMN_CONTENT_TYPE + "=? AND "
                            + DriveDBHelper.DRIVE_COLUMN_AREA_ID + "=''",
                    new String[]{"folder"});
            if (cursor == null) {
                return resources;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    DriveResource dr = new DriveResource();

                    dr.setUniqueId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_UNIQUE_ID)));
                    dr.setUserId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_USER_ID)));
                    dr.setContainerId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_CONTAINER_ID)));
                    dr.setResourceId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_RESOURCE_ID)));
                    dr.setName(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_NAME)));
                    dr.setType(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_TYPE)));
                    dr.setContentType("folder");
                    dr.setMimeType("application/vnd.google-apps.folder");
                    dr.setAreaId("");
                    dr.setSize("0");
                    dr.setLatitude("");
                    dr.setLongitude("");
                    dr.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_CREATED_MILLIS)));

                    resources.put(dr.getName(), dr);
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return resources;
    }

    public List<DriveResource> fetchImageResources(AreaElement areaElement) {
        List<DriveResource> resources = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + DriveDBHelper.DRIVE_TABLE_NAME + " WHERE "
                            + DriveDBHelper.DRIVE_COLUMN_AREA_ID + "=? AND " + DriveDBHelper.DRIVE_COLUMN_TYPE + "='file' AND "
                            + DriveDBHelper.DRIVE_COLUMN_CONTENT_TYPE + "='Image'",
                    new String[]{areaElement.getUniqueId()});
            if (cursor == null) {
                return null;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    DriveResource resource = new DriveResource();
                    resource.setUniqueId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_UNIQUE_ID)));
                    resource.setAreaId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_AREA_ID)));
                    resource.setUserId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_USER_ID)));
                    resource.setContainerId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_CONTAINER_ID)));
                    resource.setResourceId(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_RESOURCE_ID)));
                    resource.setName(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_NAME)));
                    resource.setType(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_TYPE)));
                    resource.setContentType(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_CONTENT_TYPE)));
                    resource.setMimeType(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_MIME_TYPE)));
                    resource.setSize(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_SIZE)));
                    resource.setLatitude(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_LATITUDE)));
                    resource.setLongitude(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_LONGITUDE)));
                    resource.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(DriveDBHelper.DRIVE_COLUMN_CREATED_MILLIS)));

                    resources.add(resource);
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return resources;
    }

    private JSONObject preparePostParams(String queryType, DriveResource dr) {
        JSONObject postParams = new JSONObject();
        try {
            postParams.put("requestType", "DriveMaster");
            postParams.put("query_type", queryType);
            postParams.put("device_id", AndroidSystemUtil.getDeviceId());
            postParams.put(DriveDBHelper.DRIVE_COLUMN_AREA_ID, dr.getAreaId());
            postParams.put(DriveDBHelper.DRIVE_COLUMN_USER_ID, dr.getUserId());
            postParams.put(DriveDBHelper.DRIVE_COLUMN_UNIQUE_ID, dr.getUniqueId());
            postParams.put(DriveDBHelper.DRIVE_COLUMN_CONTAINER_ID, dr.getContainerId());
            postParams.put(DriveDBHelper.DRIVE_COLUMN_RESOURCE_ID, dr.getResourceId());
            postParams.put(DriveDBHelper.DRIVE_COLUMN_NAME, dr.getName());
            postParams.put(DriveDBHelper.DRIVE_COLUMN_TYPE, dr.getType());
            postParams.put(DriveDBHelper.DRIVE_COLUMN_CONTENT_TYPE, dr.getContentType());
            postParams.put(DriveDBHelper.DRIVE_COLUMN_MIME_TYPE, dr.getMimeType());
            postParams.put(DriveDBHelper.DRIVE_COLUMN_SIZE, dr.getSize());
            postParams.put(DriveDBHelper.DRIVE_COLUMN_LATITUDE, dr.getLatitude());
            postParams.put(DriveDBHelper.DRIVE_COLUMN_LONGITUDE, dr.getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
    }

    public void deleteResourcesByAreaId(String areaId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + DriveDBHelper.DRIVE_TABLE_NAME + " WHERE "
                + DriveDBHelper.DRIVE_COLUMN_AREA_ID + " = '" + areaId + "'");
        db.close();
    }

    public void deleteResourceByResourceId(String resourceId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + DriveDBHelper.DRIVE_TABLE_NAME + " WHERE "
                + DriveDBHelper.DRIVE_COLUMN_RESOURCE_ID + " = '" + resourceId + "'");
        db.close();

        // Delete from server.
        DriveResource resource = new DriveResource();
        resource.setResourceId(resourceId);
        JSONObject postParams = this.preparePostParams("delete", resource);
        new LMSRestAsyncTask().execute(postParams);
    }

    public void deleteResource(DriveResource resource) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + DriveDBHelper.DRIVE_TABLE_NAME + " WHERE "
                + DriveDBHelper.DRIVE_COLUMN_AREA_ID + "='" + resource.getAreaId() + "' and "
                + DriveDBHelper.DRIVE_COLUMN_NAME + "='" + resource.getName() + "'");
        db.close();
    }


    public void deleteDriveElementsLocally() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(DriveDBHelper.DRIVE_TABLE_NAME, "1", null);
        db.close();
    }

    public void setCompletionCallback(AsyncTaskCallback callback) {
        this.callback = callback;
    }

    public void finalizeTaskCompletion() {
        this.callback.taskCompleted("");
    }


}