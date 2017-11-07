package lm.pkp.com.landmap.area.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.sync.LMSRestAsyncTask;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.util.AndroidSystemUtil;

public class AreaDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    private Context localContext = null;

    public static final String AREA_TABLE_NAME = "area_master";
    public static final String AREA_COLUMN_NAME = "name";
    public static final String AREA_COLUMN_DESCRIPTION = "desc";
    public static final String AREA_COLUMN_CREATED_BY = "created_by";
    public static final String AREA_COLUMN_CURRENT_OWNER = "current_owner";
    public static final String AREA_COLUMN_OWNERSHIP_TYPE = "ownership_type";
    public static final String AREA_COLUMN_CENTER_LAT = "center_lat";
    public static final String AREA_COLUMN_CENTER_LON = "center_lon";
    public static final String AREA_COLUMN_MEASURE_SQ_FT = "measure_sq_ft";
    public static final String AREA_COLUMN_UNIQUE_ID = "unique_id";
    public static final String AREA_COLUMN_TAGS = "tags";

    public AreaDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        localContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + AREA_TABLE_NAME + "(" +
                        AREA_COLUMN_NAME + " text," +
                        AREA_COLUMN_DESCRIPTION + " text," +
                        AREA_COLUMN_CREATED_BY + " text," +
                        AREA_COLUMN_CURRENT_OWNER + " text," +
                        AREA_COLUMN_OWNERSHIP_TYPE + " text," +
                        AREA_COLUMN_CENTER_LAT + " text, " +
                        AREA_COLUMN_CENTER_LON + " text, " +
                        AREA_COLUMN_MEASURE_SQ_FT + " text, " +
                        AREA_COLUMN_UNIQUE_ID + " text, " +
                        AREA_COLUMN_TAGS + " text )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AREA_TABLE_NAME);
        onCreate(db);
    }

    public AreaElement insertAreaLocally() {
        SQLiteDatabase db = this.getWritableDatabase();

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

        contentValues.put(AREA_COLUMN_OWNERSHIP_TYPE, "self");
        ae.setOwnershipType("self");

        contentValues.put(AREA_COLUMN_CURRENT_OWNER, createdBy);
        ae.setCurrentOwner(createdBy);

        contentValues.put(AREA_COLUMN_UNIQUE_ID, uniqueId);
        ae.setUniqueId(uniqueId);

        contentValues.put(AREA_COLUMN_TAGS, "");
        ae.setTags("");

        contentValues.put(AREA_COLUMN_CENTER_LAT, "0.0");
        ae.setCenterLat(0.0);

        contentValues.put(AREA_COLUMN_CENTER_LON, "0.0");
        ae.setCenterLon(0.0);

        contentValues.put(AREA_COLUMN_MEASURE_SQ_FT, "0");
        ae.setMeasureSqFt(0);

        db.insert(AREA_TABLE_NAME, null, contentValues);

        db.close();
        return ae;
    }

    public void insertAreaToServer(AreaElement ae){
        new LMSRestAsyncTask().execute(preparePostParams("insert",ae));
    }

    public AreaElement insertAreaFromServer(AreaElement ae) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(AREA_COLUMN_UNIQUE_ID, ae.getUniqueId());
        contentValues.put(AREA_COLUMN_NAME, ae.getName());
        contentValues.put(AREA_COLUMN_DESCRIPTION, ae.getDescription());
        contentValues.put(AREA_COLUMN_CREATED_BY, ae.getCreatedBy());
        contentValues.put(AREA_COLUMN_CURRENT_OWNER, ae.getCurrentOwner());
        contentValues.put(AREA_COLUMN_OWNERSHIP_TYPE, ae.getOwnershipType());
        contentValues.put(AREA_COLUMN_TAGS, ae.getTags());
        contentValues.put(AREA_COLUMN_CENTER_LAT, ae.getCenterLat() + "");
        contentValues.put(AREA_COLUMN_CENTER_LON, ae.getCenterLon() + "");
        contentValues.put(AREA_COLUMN_MEASURE_SQ_FT, ae.getMeasureSqFt() + "");

        db.insert(AREA_TABLE_NAME, null, contentValues);
        db.close();
        return ae;
    }

    public boolean updateArea(AreaElement ae) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(AREA_COLUMN_UNIQUE_ID, ae.getUniqueId());
        contentValues.put(AREA_COLUMN_NAME, ae.getName());
        contentValues.put(AREA_COLUMN_DESCRIPTION, ae.getDescription());
        contentValues.put(AREA_COLUMN_CENTER_LAT, ae.getCenterLat());
        contentValues.put(AREA_COLUMN_CENTER_LON, ae.getCenterLon());
        contentValues.put(AREA_COLUMN_OWNERSHIP_TYPE, ae.getOwnershipType());
        contentValues.put(AREA_COLUMN_MEASURE_SQ_FT, ae.getMeasureSqFt() + "");
        contentValues.put(AREA_COLUMN_CURRENT_OWNER, ae.getCurrentOwner());
        contentValues.put(AREA_COLUMN_CREATED_BY, ae.getCreatedBy());

        try{
            Location areaLocation = new Location("");
            areaLocation.setLatitude(ae.getCenterLat());
            areaLocation.setLongitude(ae.getCenterLon());

            StringBuffer buf = new StringBuffer();
            Geocoder geocoder = new Geocoder(localContext,Locale.ENGLISH);
            List<Address> addresses = geocoder.getFromLocation(areaLocation.getLatitude(), areaLocation.getLongitude(), 1);
            for (int i = 0; i < addresses.size(); i++) {
                Address address = addresses.get(i);
                int maxLine = address.getMaxAddressLineIndex();
                for (int j = 0; j <= maxLine; j++) {
                    buf.append(address.getAddressLine(j));
                    if(j != maxLine){
                        buf.append(",");
                    }
                }
            }
            contentValues.put(AREA_COLUMN_TAGS, buf.toString());
        }catch (Exception e){
            // Do nothing if fails.
        }

        db.update(AREA_TABLE_NAME, contentValues, AREA_COLUMN_UNIQUE_ID + " = ? ", new String[]{ae.getUniqueId()});
        new LMSRestAsyncTask().execute(preparePostParams("update", ae));
        db.close();

        return true;
    }

    public boolean updateAreaNonGeo(AreaElement ae) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(AREA_COLUMN_UNIQUE_ID, ae.getUniqueId());
        contentValues.put(AREA_COLUMN_NAME, ae.getName());
        contentValues.put(AREA_COLUMN_DESCRIPTION, ae.getDescription());
        contentValues.put(AREA_COLUMN_CENTER_LAT, ae.getCenterLat());
        contentValues.put(AREA_COLUMN_CENTER_LON, ae.getCenterLon());
        contentValues.put(AREA_COLUMN_OWNERSHIP_TYPE, ae.getOwnershipType());
        contentValues.put(AREA_COLUMN_MEASURE_SQ_FT, ae.getMeasureSqFt() + "");
        contentValues.put(AREA_COLUMN_CURRENT_OWNER, ae.getCurrentOwner());
        contentValues.put(AREA_COLUMN_CREATED_BY, ae.getCreatedBy());
        contentValues.put(AREA_COLUMN_TAGS, ae.getTags());

        db.update(AREA_TABLE_NAME, contentValues, AREA_COLUMN_UNIQUE_ID + " = ? ", new String[]{ae.getUniqueId()});
        new LMSRestAsyncTask().execute(preparePostParams("update", ae));
        db.close();

        return true;
    }
    public Integer deleteArea(AreaElement ae) {
        SQLiteDatabase db = this.getWritableDatabase();

        PositionsDBHelper pdb = new PositionsDBHelper(localContext);
        pdb.deletePositionByUniqueAreaId(ae.getUniqueId());

        int delete = db.delete(AREA_TABLE_NAME,
                AREA_COLUMN_UNIQUE_ID + " = ? ",
                new String[]{ae.getUniqueId()});

        JSONObject areaPostParams = preparePostParams("delete", ae);
        new LMSRestAsyncTask().execute(areaPostParams);

        db.close();
        return delete;
    }

    public ArrayList<AreaElement> getAllAreas() {
        ArrayList<AreaElement> allAreas = new ArrayList<AreaElement>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + AREA_TABLE_NAME, null);
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
                    ae.setOwnershipType(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_OWNERSHIP_TYPE)));
                    ae.setUniqueId(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_UNIQUE_ID)));
                    ae.setTags(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_TAGS)));
                    ae.setCenterLon(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CENTER_LON))));
                    ae.setCenterLat(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CENTER_LAT))));
                    ae.setMeasureSqFt(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_MEASURE_SQ_FT))));
                    ae.setCurrentOwner(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CURRENT_OWNER)));

                    allAreas.add(ae);
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

    public AreaElement getAreaByName(String name) {
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        AreaElement ae = new AreaElement();
        try {
            cursor = db.rawQuery("SELECT * FROM " + AREA_TABLE_NAME + " WHERE " + AREA_COLUMN_NAME + "=?",
                    new String[]{name});
            if (cursor == null) {
                return ae;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                ae.setUniqueId(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_UNIQUE_ID)));
                ae.setName(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_NAME)));
                ae.setDescription(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_DESCRIPTION)));
                ae.setCreatedBy(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CREATED_BY)));
                ae.setCurrentOwner(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CURRENT_OWNER)));
                ae.setCenterLon(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CENTER_LON))));
                ae.setCenterLat(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CENTER_LAT))));
                ae.setMeasureSqFt(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_MEASURE_SQ_FT))));
                ae.setOwnershipType(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_OWNERSHIP_TYPE)));
                ae.setTags(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_TAGS)));

                PositionsDBHelper pdb = new PositionsDBHelper(localContext);
                ae.setPositions(pdb.getAllPositionForArea(ae));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return ae;
    }

    public AreaElement getAreaByUid(String uid) {
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        AreaElement ae = new AreaElement();
        try {
            cursor = db.rawQuery("SELECT * FROM " + AREA_TABLE_NAME + " WHERE " + AREA_COLUMN_UNIQUE_ID + "=?",
                    new String[]{uid});
            if (cursor == null) {
                return ae;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                ae.setUniqueId(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_UNIQUE_ID)));
                ae.setName(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_NAME)));
                ae.setDescription(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_DESCRIPTION)));
                ae.setCreatedBy(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CREATED_BY)));
                ae.setCurrentOwner(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CURRENT_OWNER)));
                ae.setCenterLon(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CENTER_LON))));
                ae.setCenterLat(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CENTER_LAT))));
                ae.setMeasureSqFt(new Double(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_MEASURE_SQ_FT))));
                ae.setOwnershipType(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_OWNERSHIP_TYPE)));
                ae.setTags(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_TAGS)));

                PositionsDBHelper pdb = new PositionsDBHelper(localContext);
                ae.setPositions(pdb.getAllPositionForArea(ae));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        db.close();
        return ae;
    }

    private JSONObject preparePostParams(String queryType, AreaElement ae) {
        JSONObject postParams = new JSONObject();
        try {
            postParams.put("requestType", "AreaMaster");
            postParams.put("queryType", queryType);
            postParams.put("deviceID", AndroidSystemUtil.getDeviceId());
            postParams.put("center_lon", ae.getCenterLon());
            postParams.put("center_lat", ae.getCenterLat());
            postParams.put("desc", ae.getDescription());
            postParams.put("name", ae.getName());
            postParams.put("created_by", ae.getCreatedBy());
            postParams.put("unique_id", ae.getUniqueId());
            postParams.put("own_type", ae.getOwnershipType());
            postParams.put("msqft", ae.getMeasureSqFt());
            postParams.put("cown", ae.getCurrentOwner());
            postParams.put("tags", ae.getTags());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postParams;
    }

    public void deleteAreasLocally() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(AREA_TABLE_NAME, "1", null);
        db.close();
    }

}