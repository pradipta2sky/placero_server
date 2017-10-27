package lm.pkp.com.landmap.position;


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

import lm.pkp.com.landmap.sync.LandMapAsyncRestSync;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.util.AndroidSystemUtil;

public class PositionsDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    public static final String POSITION_TABLE_NAME = "position_master";
    private final LandMapAsyncRestSync syncher = new LandMapAsyncRestSync();

    public static final String POSITION_COLUMN_NAME = "name";
    public static final String POSITION_COLUMN_DESCRIPTION = "desc";
    public static final String POSITION_COLUMN_LAT = "lat";
    public static final String POSITION_COLUMN_LON = "lon";
    public static final String POSITION_COLUMN_TAGS = "tags";
    private static final String POSITION_COLUMN_UNIQUE_AREA_ID = "uniqueAreaId";
    private static final String POSITION_COLUMN_UNIQUE_ID = "uniqueId";

    public PositionsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + POSITION_TABLE_NAME + "(" +
                        POSITION_COLUMN_NAME + " text," +
                        POSITION_COLUMN_DESCRIPTION + " text," +
                        POSITION_COLUMN_LAT + " text, " +
                        POSITION_COLUMN_LON + " text," +
                        POSITION_COLUMN_UNIQUE_AREA_ID + " text," +
                        POSITION_COLUMN_UNIQUE_ID + " text," +
                        POSITION_COLUMN_TAGS + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + POSITION_TABLE_NAME);
        onCreate(db);
    }

    public PositionElement insertPosition(String name, String desc, String lat, String lon, String tags,String uniqueAreadId) {
        SQLiteDatabase db = this.getWritableDatabase();
        PositionElement pe = new PositionElement();

        ContentValues contentValues = new ContentValues();

        contentValues.put(POSITION_COLUMN_NAME, name);
        pe.setName(name);

        contentValues.put(POSITION_COLUMN_DESCRIPTION, desc);
        pe.setDescription(desc);

        contentValues.put(POSITION_COLUMN_LAT, lat);
        pe.setLat(new Double(lat));

        contentValues.put(POSITION_COLUMN_LON, lon);
        pe.setLon(new Double(lon));

        contentValues.put(POSITION_COLUMN_TAGS, tags);
        pe.setTags(tags);

        contentValues.put(POSITION_COLUMN_UNIQUE_AREA_ID, uniqueAreadId);
        pe.setUniqueAreaId(uniqueAreadId);

        String uniqueId = UUID.randomUUID().toString();
        contentValues.put(POSITION_COLUMN_UNIQUE_ID,uniqueId);
        pe.setUniqueId(uniqueId);

        db.insert(POSITION_TABLE_NAME, null, contentValues);

        JSONObject postParams = preparePostParams("insert",pe.getUniqueId(), pe.getUniqueAreaId(), pe.getName(), pe.getDescription(),
                pe.getLon() + "", pe.getLat() + "",pe.getTags(),AndroidSystemUtil.getDeviceId());
        syncher.execute(postParams);
        db.close();
        return pe;
    }

    public PositionElement insertPositionFromServer(PositionElement pe) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(POSITION_COLUMN_UNIQUE_ID,pe.getUniqueId());
        contentValues.put(POSITION_COLUMN_UNIQUE_AREA_ID, pe.getUniqueAreaId());
        contentValues.put(POSITION_COLUMN_NAME, pe.getName());
        contentValues.put(POSITION_COLUMN_DESCRIPTION, pe.getDescription());
        contentValues.put(POSITION_COLUMN_LAT, pe.getLat());
        contentValues.put(POSITION_COLUMN_LON, pe.getLon());
        contentValues.put(POSITION_COLUMN_TAGS, pe.getTags());

        db.insert(POSITION_TABLE_NAME, null, contentValues);
        db.close();
        return pe;
    }

    public void insertPositionToServer(PositionElement pe) {
        JSONObject postParams = preparePostParams("insert", pe.getUniqueId(), pe.getUniqueAreaId(), pe.getName(), pe.getDescription(),
                pe.getLon() + "", pe.getLat() + "", pe.getTags(), AndroidSystemUtil.getDeviceId());
        syncher.execute(postParams);
    }

    public Integer deletePosition(PositionElement pe) {
        SQLiteDatabase db = this.getWritableDatabase();
        String uniqueId = pe.getUniqueId();
        int delete = db.delete(POSITION_TABLE_NAME,
                POSITION_COLUMN_UNIQUE_ID + " = ? ",
                new String[]{uniqueId});
        JSONObject postParams = preparePostParams("delete", uniqueId);
        syncher.execute(postParams);
        db.close();
        return delete;
    }

    public void deletePositionByName(String pName, String uniqueAreaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+POSITION_TABLE_NAME+" WHERE "
                + POSITION_COLUMN_UNIQUE_AREA_ID +" = "+uniqueAreaId
                + " AND "+POSITION_COLUMN_NAME+" = '"+pName+"'");
        JSONObject postParams = preparePostParams("deleteByName", uniqueAreaId, pName);
        syncher.execute(postParams);
        db.close();
    }

    public void deletePositionByUniqueId(String uniqueAreaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + POSITION_TABLE_NAME + " WHERE "
                + POSITION_COLUMN_UNIQUE_AREA_ID + " = '" + uniqueAreaId + "'");
        JSONObject postParams = preparePostParams("deleteByUniqueAreaId", uniqueAreaId, null);
        syncher.execute(postParams);
        db.close();
    }

    public ArrayList<PositionElement> getAllPositionForArea(AreaElement ae) {
        ArrayList<PositionElement> pes = new ArrayList<PositionElement>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + POSITION_TABLE_NAME + " WHERE " + POSITION_COLUMN_UNIQUE_AREA_ID + "=?",
                new String[]{ae.getUniqueId() + ""});
        if(cursor != null){
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                PositionElement pe = new PositionElement();

                pe.setName(cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_NAME)));
                pe.setDescription(cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_DESCRIPTION)));

                String latStr = cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_LAT));
                pe.setLat(Double.parseDouble(latStr));

                String lonStr = cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_LON));
                pe.setLon(Double.parseDouble(lonStr));

                pe.setTags(cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_TAGS)));
                pes.add(pe);

                cursor.moveToNext();
            }
            cursor.close();
        }
        return pes;
    }

    private JSONObject preparePostParams(String queryType,String uniqueAreaId , String positionName) {
        JSONObject postParams = preparePostParams(queryType, null, uniqueAreaId, positionName, null, null, null, null,  AndroidSystemUtil.getDeviceId());
        syncher.execute(postParams);
        return postParams;
    }

    private JSONObject preparePostParams(String queryType,String uniqueId) {
        JSONObject postParams = preparePostParams(queryType, uniqueId, null, null, null, null, null, null, AndroidSystemUtil.getDeviceId());
        syncher.execute(postParams);
        return postParams;
    }

    private JSONObject preparePostParams(String queryType, String uniqueId, String uniqueAreadId, String name, String desc,
                                         String lon, String lat, String tags, String deviceID) {
        JSONObject postParams = new JSONObject();
        try {
            if(deviceID==null){
                deviceID = AndroidSystemUtil.getDeviceId();
            }
            postParams.put("requestType", "PositionMaster");
            postParams.put("queryType", queryType);
            postParams.put("deviceID",deviceID);
            postParams.put("lon",lon);
            postParams.put("lat",lat);
            postParams.put("desc",desc);
            postParams.put("tags",tags);
            postParams.put("name",name);
            postParams.put("uniqueAreaId",uniqueAreadId);
            postParams.put("uniqueId",uniqueId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  postParams;
    }

    public void deletePositionsLocally() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(POSITION_TABLE_NAME, "1", null);
        db.close();
    }

}