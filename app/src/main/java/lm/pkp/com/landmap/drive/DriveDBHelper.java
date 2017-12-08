package lm.pkp.com.landmap.drive;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lm.pkp.com.landmap.area.model.AreaElement;
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
    public static final String DRIVE_COLUMN_CREATED_ON = "created_on";

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
                        DRIVE_COLUMN_USER_ID + " text," +
                        DRIVE_COLUMN_RESOURCE_ID + " text," +
                        DRIVE_COLUMN_CONTAINER_ID + " text," +
                        DRIVE_COLUMN_NAME + " text," +
                        DRIVE_COLUMN_TYPE + " text," +
                        DRIVE_COLUMN_CONTENT_TYPE + " text," +
                        DRIVE_COLUMN_MIME_TYPE + " text," +
                        DRIVE_COLUMN_LATITUDE + " text," +
                        DRIVE_COLUMN_LONGITUDE + " text," +
                        DRIVE_COLUMN_CREATED_ON + " text," +
                        DRIVE_COLUMN_SIZE + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DRIVE_TABLE_NAME);
        this.onCreate(db);
    }

    public void insertResourceLocally(DriveResource dr) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DRIVE_COLUMN_UNIQUE_ID, dr.getUniqueId());
        contentValues.put(DRIVE_COLUMN_AREA_ID, dr.getAreaId());
        contentValues.put(DRIVE_COLUMN_USER_ID, dr.getUserId());
        contentValues.put(DRIVE_COLUMN_RESOURCE_ID, dr.getResourceId());
        contentValues.put(DRIVE_COLUMN_CONTAINER_ID, dr.getContainerId());
        contentValues.put(DRIVE_COLUMN_NAME, dr.getName());
        contentValues.put(DRIVE_COLUMN_TYPE, dr.getType());
        contentValues.put(DRIVE_COLUMN_CONTENT_TYPE, dr.getContentType());
        contentValues.put(DRIVE_COLUMN_MIME_TYPE, dr.getMimeType());
        contentValues.put(DRIVE_COLUMN_SIZE, dr.getSize());
        contentValues.put(DRIVE_COLUMN_LATITUDE, dr.getLatitude());
        contentValues.put(DRIVE_COLUMN_LONGITUDE, dr.getLongitude());
        contentValues.put(DRIVE_COLUMN_CREATED_ON, dr.getCreatedOnMillis());

        db.insert(DRIVE_TABLE_NAME, null, contentValues);
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
        contentValues.put(DRIVE_COLUMN_UNIQUE_ID, dr.getUniqueId());
        contentValues.put(DRIVE_COLUMN_AREA_ID, dr.getAreaId());
        contentValues.put(DRIVE_COLUMN_USER_ID, dr.getUserId());
        contentValues.put(DRIVE_COLUMN_RESOURCE_ID, dr.getResourceId());
        contentValues.put(DRIVE_COLUMN_CONTAINER_ID, dr.getContainerId());
        contentValues.put(DRIVE_COLUMN_NAME, dr.getName());
        contentValues.put(DRIVE_COLUMN_TYPE, dr.getType());
        contentValues.put(DRIVE_COLUMN_CONTENT_TYPE, dr.getContentType());
        contentValues.put(DRIVE_COLUMN_MIME_TYPE, dr.getMimeType());
        contentValues.put(DRIVE_COLUMN_SIZE, dr.getSize());
        contentValues.put(DRIVE_COLUMN_LATITUDE, dr.getLatitude());
        contentValues.put(DRIVE_COLUMN_LONGITUDE, dr.getLongitude());
        contentValues.put(DRIVE_COLUMN_CREATED_ON, dr.getCreatedOnMillis());

        db.insert(DRIVE_TABLE_NAME, null, contentValues);
        db.close();
        return dr;
    }

    public ArrayList<DriveResource> getDriveResourcesByAreaId(String aid) {
        ArrayList<DriveResource> allResources = new ArrayList<DriveResource>();
        SQLiteDatabase db = getReadableDatabase();
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
                    dr.setUserId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_USER_ID)));
                    dr.setContainerId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTAINER_ID)));
                    dr.setResourceId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_RESOURCE_ID)));
                    dr.setName(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_NAME)));
                    dr.setType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_TYPE)));
                    dr.setContentType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTENT_TYPE)));
                    dr.setMimeType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_MIME_TYPE)));
                    dr.setSize(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_SIZE)));
                    dr.setLatitude(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_LATITUDE)));
                    dr.setLongitude(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_LONGITUDE)));
                    dr.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CREATED_ON)));

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

    public DriveResource getDriveResourceByResourceId(String resourceID) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        DriveResource resource = new DriveResource();
        try {
            cursor = db.rawQuery("select * from " + DRIVE_TABLE_NAME + " WHERE "
                            + DRIVE_COLUMN_RESOURCE_ID + "=?",
                    new String[]{resourceID});
            if (cursor == null) {
                return null;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    resource.setUniqueId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_UNIQUE_ID)));
                    resource.setAreaId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_AREA_ID)));
                    resource.setUserId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_USER_ID)));
                    resource.setContainerId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTAINER_ID)));
                    resource.setResourceId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_RESOURCE_ID)));
                    resource.setName(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_NAME)));
                    resource.setType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_TYPE)));
                    resource.setContentType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTENT_TYPE)));
                    resource.setMimeType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_MIME_TYPE)));
                    resource.setSize(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_SIZE)));
                    resource.setLatitude(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_LATITUDE)));
                    resource.setLongitude(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_LONGITUDE)));
                    resource.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CREATED_ON)));
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

    public DriveResource getDriveResourceRoot(String parentName, AreaElement areaElement) {
        DriveResource childResource = new DriveResource();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + DRIVE_TABLE_NAME + " WHERE "
                            + DRIVE_COLUMN_AREA_ID + "=? AND " + DRIVE_COLUMN_TYPE + "='folder' AND "
                            + DRIVE_COLUMN_CONTENT_TYPE + "=''",
                    new String[]{areaElement.getUniqueId()});
            if (cursor == null) {
                return null;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    DriveResource resource = new DriveResource();
                    resource.setUniqueId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_UNIQUE_ID)));
                    resource.setAreaId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_AREA_ID)));
                    resource.setUserId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_USER_ID)));
                    resource.setContainerId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTAINER_ID)));
                    resource.setResourceId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_RESOURCE_ID)));
                    resource.setName(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_NAME)));
                    resource.setType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_TYPE)));
                    resource.setContentType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTENT_TYPE)));
                    resource.setMimeType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_MIME_TYPE)));
                    resource.setSize(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_SIZE)));
                    resource.setLatitude(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_LATITUDE)));
                    resource.setLongitude(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_LONGITUDE)));
                    resource.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CREATED_ON)));

                    Map<String, DriveResource> commonResources = this.getCommonResourcesById();
                    Collection<DriveResource> commonDriveResources = commonResources.values();

                    Iterator<DriveResource> iterator = commonDriveResources.iterator();
                    while (iterator.hasNext()){
                        DriveResource commonRoot = iterator.next();
                        if(resource.getContainerId().equalsIgnoreCase(commonRoot.getResourceId())){
                            childResource = resource;
                            break;
                        }
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

    public Map<String, DriveResource> getCommonResourcesByName() {
        Map<String, DriveResource> resources = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + DRIVE_TABLE_NAME + " WHERE "
                            + DRIVE_COLUMN_CONTENT_TYPE + "=? AND "
                            + DRIVE_COLUMN_AREA_ID + "=''",
                    new String[]{"folder"});
            if (cursor == null) {
                return resources;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    DriveResource dr = new DriveResource();

                    dr.setUniqueId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_UNIQUE_ID)));
                    dr.setUserId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_USER_ID)));
                    dr.setContainerId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTAINER_ID)));
                    dr.setResourceId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_RESOURCE_ID)));
                    dr.setName(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_NAME)));
                    dr.setType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_TYPE)));
                    dr.setContentType("folder");
                    dr.setMimeType("application/vnd.google-apps.folder");
                    dr.setAreaId("");
                    dr.setSize("0");
                    dr.setLatitude("");
                    dr.setLongitude("");
                    dr.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CREATED_ON)));

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

    public Map<String, DriveResource> getCommonResourcesById() {
        Map<String, DriveResource> resources = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + DRIVE_TABLE_NAME + " WHERE "
                            + DRIVE_COLUMN_CONTENT_TYPE + "=? AND "
                            + DRIVE_COLUMN_AREA_ID + "=''",
                    new String[]{"folder"});
            if (cursor == null) {
                return resources;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    DriveResource dr = new DriveResource();

                    dr.setUniqueId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_UNIQUE_ID)));
                    dr.setUserId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_USER_ID)));
                    dr.setContainerId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTAINER_ID)));
                    dr.setResourceId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_RESOURCE_ID)));
                    dr.setName(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_NAME)));
                    dr.setType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_TYPE)));
                    dr.setContentType("folder");
                    dr.setMimeType("application/vnd.google-apps.folder");
                    dr.setAreaId("");
                    dr.setSize("0");
                    dr.setLatitude("");
                    dr.setLongitude("");
                    dr.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CREATED_ON)));

                    resources.put(dr.getResourceId(), dr);
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
            cursor = db.rawQuery("select * from " + DRIVE_TABLE_NAME + " WHERE "
                            + DRIVE_COLUMN_AREA_ID + "=? AND " + DRIVE_COLUMN_TYPE + "='file' AND "
                            + DRIVE_COLUMN_CONTENT_TYPE + "='Image'",
                    new String[]{areaElement.getUniqueId()});
            if (cursor == null) {
                return null;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    DriveResource resource = new DriveResource();
                    resource.setUniqueId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_UNIQUE_ID)));
                    resource.setAreaId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_AREA_ID)));
                    resource.setUserId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_USER_ID)));
                    resource.setContainerId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTAINER_ID)));
                    resource.setResourceId(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_RESOURCE_ID)));
                    resource.setName(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_NAME)));
                    resource.setType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_TYPE)));
                    resource.setContentType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CONTENT_TYPE)));
                    resource.setMimeType(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_MIME_TYPE)));
                    resource.setSize(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_SIZE)));
                    resource.setLatitude(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_LATITUDE)));
                    resource.setLongitude(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_LONGITUDE)));
                    resource.setCreatedOnMillis(cursor.getString(cursor.getColumnIndex(DRIVE_COLUMN_CREATED_ON)));

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
            postParams.put(DRIVE_COLUMN_AREA_ID, dr.getAreaId());
            postParams.put(DRIVE_COLUMN_USER_ID, dr.getUserId());
            postParams.put(DRIVE_COLUMN_UNIQUE_ID, dr.getUniqueId());
            postParams.put(DRIVE_COLUMN_CONTAINER_ID, dr.getContainerId());
            postParams.put(DRIVE_COLUMN_RESOURCE_ID, dr.getResourceId());
            postParams.put(DRIVE_COLUMN_NAME, dr.getName());
            postParams.put(DRIVE_COLUMN_TYPE, dr.getType());
            postParams.put(DRIVE_COLUMN_CONTENT_TYPE, dr.getContentType());
            postParams.put(DRIVE_COLUMN_MIME_TYPE, dr.getMimeType());
            postParams.put(DRIVE_COLUMN_SIZE, dr.getSize());
            postParams.put(DRIVE_COLUMN_LATITUDE, dr.getLatitude());
            postParams.put(DRIVE_COLUMN_LONGITUDE, dr.getLongitude());
            postParams.put(DRIVE_COLUMN_LONGITUDE, dr.getLongitude());
            postParams.put(DRIVE_COLUMN_CREATED_ON, dr.getCreatedOnMillis());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
    }

    public void deleteResourcesByAreaId(String areaId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + DRIVE_TABLE_NAME + " WHERE "
                + DRIVE_COLUMN_AREA_ID + " = '" + areaId + "'");
        db.close();
    }

    public void deleteResourceByResourceId(String resourceId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + DRIVE_TABLE_NAME + " WHERE "
                + DRIVE_COLUMN_RESOURCE_ID + " = '" + resourceId + "'");
        db.close();

        // Delete from server.
        DriveResource resource = new DriveResource();
        resource.setResourceId(resourceId);
        JSONObject postParams = this.preparePostParams("delete", resource);
        new LMSRestAsyncTask().execute(postParams);
    }

    public void deleteResourceLocally(DriveResource resource) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + DRIVE_TABLE_NAME + " WHERE "
                + DRIVE_COLUMN_AREA_ID + "='" + resource.getAreaId() + "' and "
                + DRIVE_COLUMN_NAME + "='" + resource.getName() + "'");
        db.close();
    }

    public void deleteResourceFromServer(DriveResource resource) {
        // Delete from server.
        JSONObject postParams = this.preparePostParams("delete", resource);
        new LMSRestAsyncTask().execute(postParams);
    }

    public void cleanLocalDriveResources() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(DRIVE_TABLE_NAME, "1", null);
        db.close();
    }

    public void setCompletionCallback(AsyncTaskCallback callback) {
        this.callback = callback;
    }

    public void finalizeTaskCompletion() {
        this.callback.taskCompleted("");
    }


}