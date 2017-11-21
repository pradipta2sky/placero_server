package lm.pkp.com.landmap.area.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.google.geo.CommonGeoHelper;
import lm.pkp.com.landmap.permission.PermissionsDBHelper;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.sync.LMSRestAsyncTask;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.util.AndroidSystemUtil;

public class AreaDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    private Context localContext;

    public static final String AREA_TABLE_NAME = "area_master";
    public static final String AREA_COLUMN_NAME = "name";
    public static final String AREA_COLUMN_DESCRIPTION = "desc";
    public static final String AREA_COLUMN_CREATED_BY = "created_by";
    public static final String AREA_COLUMN_CENTER_LAT = "center_lat";
    public static final String AREA_COLUMN_CENTER_LON = "center_lon";
    public static final String AREA_COLUMN_MEASURE_SQ_FT = "measure_sq_ft";
    public static final String AREA_COLUMN_UNIQUE_ID = "unique_id";
    public static final String AREA_COLUMN_ADDRESS = "address";
    public static final String AREA_COLUMN_TYPE = "type";

    private AsyncTaskCallback callback;

    public AreaDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.localContext = context;
    }

    public AreaDBHelper(Context context, AsyncTaskCallback callback) {
        super(context, DATABASE_NAME, null, 1);
        this.localContext = context;
        this.callback = callback;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + AREA_TABLE_NAME + "(" +
                        AREA_COLUMN_NAME + " text," +
                        AREA_COLUMN_DESCRIPTION + " text," +
                        AREA_COLUMN_CREATED_BY + " text," +
                        AREA_COLUMN_CENTER_LAT + " text, " +
                        AREA_COLUMN_CENTER_LON + " text, " +
                        AREA_COLUMN_MEASURE_SQ_FT + " text, " +
                        AREA_COLUMN_UNIQUE_ID + " text, " +
                        AREA_COLUMN_ADDRESS + " text, " +
                        AREA_COLUMN_TYPE + " text )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AREA_TABLE_NAME);
        this.onCreate(db);
    }

    public AreaElement insertAreaLocally() {
        SQLiteDatabase db = getWritableDatabase();

        AreaElement ae = new AreaElement();
        String uniqueId = UUID.randomUUID().toString();
        ae.setName("AR_" + uniqueId);

        ContentValues contentValues = new ContentValues();
        contentValues.put(AREA_COLUMN_NAME, ae.getName());

        ae.setDescription("Not Available");
        contentValues.put(AREA_COLUMN_DESCRIPTION, ae.getDescription());

        String createdBy = UserContext.getInstance().getUserElement().getEmail();
        contentValues.put(AREA_COLUMN_CREATED_BY, createdBy);
        ae.setCreatedBy(createdBy);

        contentValues.put(AREA_COLUMN_UNIQUE_ID, uniqueId);
        ae.setUniqueId(uniqueId);

        contentValues.put(AREA_COLUMN_CENTER_LAT, "0.0");
        ae.getCenterPosition().setLat(0.0);

        contentValues.put(AREA_COLUMN_CENTER_LON, "0.0");
        ae.getCenterPosition().setLon(0.0);

        contentValues.put(AREA_COLUMN_MEASURE_SQ_FT, "0");
        ae.setMeasureSqFt(0);

        contentValues.put(AREA_COLUMN_ADDRESS, "");
        ae.setAddress("");

        contentValues.put(AREA_COLUMN_TYPE, "self");
        ae.setType("self");

        db.insert(AREA_TABLE_NAME, null, contentValues);

        db.close();

        if (this.callback != null) {
            this.callback.taskCompleted(ae);
        }
        return ae;
    }

    public void insertAreaToServer(AreaElement ae) {
        LMSRestAsyncTask insertTask = new LMSRestAsyncTask(this.callback);
        insertTask.execute(this.preparePostParams("insert", ae));
    }

    public AreaElement insertAreaFromServer(AreaElement ae) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(AREA_COLUMN_UNIQUE_ID, ae.getUniqueId());
        contentValues.put(AREA_COLUMN_NAME, ae.getName());
        contentValues.put(AREA_COLUMN_DESCRIPTION, ae.getDescription());
        contentValues.put(AREA_COLUMN_CREATED_BY, ae.getCreatedBy());
        contentValues.put(AREA_COLUMN_CENTER_LAT, ae.getCenterPosition().getLat() + "");
        contentValues.put(AREA_COLUMN_CENTER_LON, ae.getCenterPosition().getLon() + "");
        contentValues.put(AREA_COLUMN_MEASURE_SQ_FT, ae.getMeasureSqFt() + "");
        contentValues.put(AREA_COLUMN_ADDRESS, ae.getAddress());
        contentValues.put(AREA_COLUMN_TYPE, ae.getType());

        db.insert(AREA_TABLE_NAME, null, contentValues);
        db.close();

        return ae;
    }

    public void updateAreaLocally(AreaElement ae) {
        SQLiteDatabase db = getWritableDatabase();
        PositionElement centerPosition = ae.getCenterPosition();

        ContentValues contentValues = new ContentValues();
        contentValues.put(AREA_COLUMN_UNIQUE_ID, ae.getUniqueId());
        contentValues.put(AREA_COLUMN_NAME, ae.getName());
        contentValues.put(AREA_COLUMN_DESCRIPTION, ae.getDescription());
        contentValues.put(AREA_COLUMN_CENTER_LAT, centerPosition.getLat());
        contentValues.put(AREA_COLUMN_CENTER_LON, centerPosition.getLon());
        contentValues.put(AREA_COLUMN_MEASURE_SQ_FT, ae.getMeasureSqFt() + "");
        contentValues.put(AREA_COLUMN_CREATED_BY, ae.getCreatedBy());
        contentValues.put(AREA_COLUMN_TYPE, ae.getType());

        String areaTags = ae.getAddress();
        if (areaTags != null && !areaTags.trim().equals("")) {
            contentValues.put(AREA_COLUMN_ADDRESS, ae.getAddress());
        } else {
            CommonGeoHelper geoHelper = CommonGeoHelper.INSTANCE;
            String address = geoHelper.getAddressByGeoLocation(this.localContext, centerPosition.getLat(), centerPosition.getLon());
            contentValues.put(AREA_COLUMN_ADDRESS, address);
        }
        ae.setAddress(contentValues.getAsString(AREA_COLUMN_ADDRESS));
        db.update(AREA_TABLE_NAME, contentValues, AREA_COLUMN_UNIQUE_ID + " = ? ", new String[]{ae.getUniqueId()});
        db.close();
    }

    public void updateAreaAttributes(AreaElement ae) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(AREA_COLUMN_NAME, ae.getName());
        contentValues.put(AREA_COLUMN_DESCRIPTION, ae.getDescription());
        contentValues.put(AREA_COLUMN_ADDRESS, ae.getAddress());

        db.update(AREA_TABLE_NAME, contentValues, AREA_COLUMN_UNIQUE_ID + " = ? ", new String[]{ae.getUniqueId()});
        db.close();
    }

    public void updateAreaOnServer(AreaElement ae) {
        LMSRestAsyncTask updateTask = new LMSRestAsyncTask(this.callback);
        updateTask.execute(this.preparePostParams("update", ae));
    }

    public void deleteArea(AreaElement ae) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(AREA_TABLE_NAME, AREA_COLUMN_UNIQUE_ID + " = ? ", new String[]{ae.getUniqueId()});
        db.close();

        PositionsDBHelper pdb = new PositionsDBHelper(this.localContext);
        pdb.deletePositionByAreaId(ae.getUniqueId());
    }

    public void deleteAreaFromServer(AreaElement ae) {
        LMSRestAsyncTask deleteTask = new LMSRestAsyncTask(this.callback);
        deleteTask.execute(this.preparePostParams("delete", ae));
    }

    public ArrayList<AreaElement> getAreas(String type) {
        ArrayList<AreaElement> allAreas = new ArrayList<AreaElement>();
        SQLiteDatabase db = getReadableDatabase();
        DriveDBHelper ddh = new DriveDBHelper(this.localContext);
        PermissionsDBHelper pdh = new PermissionsDBHelper(this.localContext);
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + AREA_TABLE_NAME + " WHERE " + AREA_COLUMN_TYPE + "=?",
                    new String[]{type});
            if (cursor == null) {
                return allAreas;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    AreaElement ae = new AreaElement();
                    ae.setName(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_NAME)));
                    ae.setDescription(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_DESCRIPTION)));
                    ae.setCreatedBy(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CREATED_BY)));
                    ae.setUniqueId(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_UNIQUE_ID)));
                    ae.getCenterPosition().setLat(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CENTER_LAT))));
                    ae.getCenterPosition().setLon(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CENTER_LON))));
                    ae.setMeasureSqFt(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_MEASURE_SQ_FT))));
                    ae.setAddress(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_ADDRESS)));
                    ae.setType(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_TYPE)));

                    ae.getMediaResources().addAll(ddh.getDriveResourcesByAreaId(ae.getUniqueId()));
                    allAreas.add(ae);

                    ae.setUserPermissions(pdh.fetchPermissionsByAreaId(ae.getUniqueId()));
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return allAreas;
    }

    private JSONObject preparePostParams(String queryType, AreaElement ae) {
        JSONObject postParams = new JSONObject();
        try {
            postParams.put("requestType", "AreaMaster");
            postParams.put("queryType", queryType);
            postParams.put("deviceID", AndroidSystemUtil.getDeviceId());
            postParams.put("center_lon", ae.getCenterPosition().getLon());
            postParams.put("center_lat", ae.getCenterPosition().getLat());
            postParams.put("desc", ae.getDescription());
            postParams.put("name", ae.getName());
            postParams.put("created_by", ae.getCreatedBy());
            postParams.put("unique_id", ae.getUniqueId());
            postParams.put("msqft", ae.getMeasureSqFt());
            postParams.put("address", ae.getAddress());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
    }

    public void deleteAreasLocally() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(AREA_TABLE_NAME, "1", null);
        db.close();
    }

    public void deletePublicAreas() {
        ArrayList<AreaElement> publicAreas = this.getAreas("public");

        PositionsDBHelper pdh = new PositionsDBHelper(this.localContext);
        DriveDBHelper ddh = new DriveDBHelper(this.localContext);
        PermissionsDBHelper pmh = new PermissionsDBHelper(this.localContext);

        SQLiteDatabase db = getWritableDatabase();
        for (int i = 0; i < publicAreas.size(); i++) {
            String areaId = publicAreas.get(i).getUniqueId();
            db.delete(AREA_TABLE_NAME, AREA_COLUMN_UNIQUE_ID + "=? AND "
                    + AREA_COLUMN_TYPE + "=?", new String[]{areaId, "public"});
            pdh.deletePositionByAreaId(areaId);
            ddh.deleteResourcesByAreaId(areaId);
            pmh.deletePermissionsByAreaId(areaId);
        }
        db.close();
    }

}