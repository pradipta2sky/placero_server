package lm.pkp.com.landmap.area;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
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

import lm.pkp.com.landmap.sync.LandMapAsyncRestSync;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.util.AndroidSystemUtil;

public class AreaDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    private Context localContext = null;

    public static final String AREA_TABLE_NAME = "area_master";
    public static final String AREA_COLUMN_ID = "id";
    public static final String AREA_COLUMN_NAME = "name";
    public static final String AREA_COLUMN_DESCRIPTION = "desc";
    public static final String AREA_COLUMN_CREATED_BY = "created_by";
    public static final String AREA_COLUMN_CENTER_LAT = "center_lat";
    public static final String AREA_COLUMN_CENTER_LON = "center_lon";
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
                        AREA_COLUMN_ID + " integer primary key, " +
                        AREA_COLUMN_NAME + " text," +
                        AREA_COLUMN_DESCRIPTION + " text," +
                        AREA_COLUMN_CREATED_BY + " text," +
                        AREA_COLUMN_CENTER_LAT + " text, " +
                        AREA_COLUMN_CENTER_LON + " text, " +
                        AREA_COLUMN_UNIQUE_ID + " text, " +
                        AREA_COLUMN_TAGS + " text )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AREA_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertArea(String name, String desc, String centerLat, String centerLon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AREA_COLUMN_NAME, name);
        contentValues.put(AREA_COLUMN_DESCRIPTION, desc);
        contentValues.put(AREA_COLUMN_CENTER_LAT, centerLat);
        contentValues.put(AREA_COLUMN_CENTER_LON, centerLon);
        contentValues.put(AREA_COLUMN_CREATED_BY, UserContext.getInstance().getUserElement().getEmail());

        String uniqueId = UUID.randomUUID().toString();
        contentValues.put(AREA_COLUMN_UNIQUE_ID, uniqueId);
        contentValues.put(AREA_COLUMN_TAGS, "");

        db.insert(AREA_TABLE_NAME, null, contentValues);

        JSONObject postParams = preparePostParams("insert", null, centerLon, centerLat, desc, name, null, uniqueId);
        new LandMapAsyncRestSync().execute(postParams);

        return true;
    }

    private JSONObject preparePostParams(String queryType, String unique_id) {
        JSONObject postParams = new JSONObject();
        postParams = preparePostParams(queryType, null, null, null, null, null, AndroidSystemUtil.getDeviceId(), unique_id);
        new LandMapAsyncRestSync().execute(postParams);
        return postParams;
    }

    private JSONObject preparePostParams(String queryType, Integer id, String centerlon, String centerlat,
                                         String desc, String name, String deviceID, String unique_id) {
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

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + AREA_TABLE_NAME + " where " + AREA_COLUMN_ID + "=" + id + "", null);
        return res;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, AREA_TABLE_NAME);
        return numRows;
    }

    public boolean updateArea(AreaElement ae) {
        SQLiteDatabase db = this.getWritableDatabase();
        String unique_id = getUniqueId(db, ae.getId());

        ContentValues contentValues = new ContentValues();
        contentValues.put(AREA_COLUMN_NAME, ae.getName());
        contentValues.put(AREA_COLUMN_DESCRIPTION, ae.getDescription());
        contentValues.put(AREA_COLUMN_CENTER_LAT, ae.getCenterLat());
        contentValues.put(AREA_COLUMN_CENTER_LON, ae.getCenterLon());
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

        db.update(AREA_TABLE_NAME, contentValues, AREA_COLUMN_ID + " = ? ", new String[]{Integer.toString(ae.getId())});

        JSONObject postParams = preparePostParams("update", ae.getId(), ae.getCenterLon() + "",
                ae.getCenterLat() + "", ae.getDescription(), ae.getName(), null, unique_id);
        new LandMapAsyncRestSync().execute(postParams);

        return true;
    }

    private String getUniqueId(SQLiteDatabase db, Integer id) {
        Cursor data = getData(id);
        data.moveToFirst();
        return data.getString(data.getColumnIndex(AREA_COLUMN_UNIQUE_ID));
    }

    public Integer deleteArea(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String uniqueId = getUniqueId(db, id);
        PositionsDBHelper pdb = new PositionsDBHelper(localContext);
        pdb.deletePositionByUniqueId(uniqueId);
        int delete = db.delete(AREA_TABLE_NAME,
                AREA_COLUMN_ID + " = ? ",
                new String[]{Integer.toString(id)});
        JSONObject postParams = preparePostParams("delete", uniqueId);
        new LandMapAsyncRestSync().execute(postParams);
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
                    ae.setId(cursor.getInt(cursor.getColumnIndex(AREA_COLUMN_ID)));
                    ae.setName(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_NAME)));
                    ae.setDescription(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_DESCRIPTION)));
                    ae.setCreatedBy(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CREATED_BY)));
                    ae.setUniqueId(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_UNIQUE_ID)));
                    ae.setTags(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_TAGS)));

                    allAreas.add(ae);
                    cursor.moveToNext();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return allAreas;
    }

    public AreaElement getAreaByName(String name) {
        Cursor cursor = null;
        AreaElement ae = new AreaElement();
        try {
            cursor = this.getReadableDatabase().rawQuery("SELECT * FROM " + AREA_TABLE_NAME + " WHERE " + AREA_COLUMN_NAME + "=?",
                    new String[]{name});
            if (cursor == null) {
                return ae;
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                ae.setId(cursor.getInt(cursor.getColumnIndex(AREA_COLUMN_ID)));
                ae.setName(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_NAME)));
                ae.setDescription(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_DESCRIPTION)));
                ae.setCreatedBy(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_CREATED_BY)));
                ae.setUniqueId(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_UNIQUE_ID)));
                ae.setTags(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_TAGS)));

                PositionsDBHelper pdb = new PositionsDBHelper(localContext);
                ae.setPositions(pdb.getAllPositionForArea(ae));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ae;
    }

}