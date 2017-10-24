package lm.pkp.com.landmap;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class PositionsDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    public static final String POSITION_TABLE_NAME = "position_master";

    public static final String POSITION_COLUMN_ID = "id";
    public static final String POSITION_COLUMN_AREA_ID = "area_id";
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
                        POSITION_COLUMN_ID + " integer primary key, " +
                        POSITION_COLUMN_AREA_ID + " integer, " +
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

    public PositionElement insertPosition(Integer areaId, String name, String desc, String lat, String lon, String tags
            ,String uniqueAreadId , String uniqueId) {
        SQLiteDatabase db = this.getWritableDatabase();
        PositionElement pe = new PositionElement();

        ContentValues contentValues = new ContentValues();
        contentValues.put(POSITION_COLUMN_AREA_ID, areaId);
        pe.setAreaId(areaId);

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

        contentValues.put(POSITION_COLUMN_UNIQUE_AREA_ID,uniqueAreadId);
        pe.setUniqueAreaId(uniqueAreadId);

        contentValues.put(POSITION_COLUMN_UNIQUE_ID,uniqueId);
        pe.setUniqueId(uniqueId);

        long id = db.insert(POSITION_TABLE_NAME, null, contentValues);
        pe.setId(id);

        JSONObject postParams = preparePostParams("insert",null,areaId,lon,lat,desc,tags,name,null,uniqueAreadId,uniqueId);
        new LandMapAsyncRestSync().execute(postParams);
        return pe;
    }
    private JSONObject preparePostParams(String queryType,String uniqueAreaId , String positionName) {
        JSONObject postParams = preparePostParams(queryType,null,null,null,null,null,null,positionName,getDeviceId(),uniqueAreaId,null);
        new LandMapAsyncRestSync().execute(postParams);
        return  postParams;
    }

    private JSONObject preparePostParams(String queryType,String unique_id) {
        JSONObject postParams = preparePostParams(queryType,null,null,null,null,null,null,null,getDeviceId(),null,unique_id);
        new LandMapAsyncRestSync().execute(postParams);
        return  postParams;
    }

    private JSONObject preparePostParams(String queryType , Integer id ,Integer areaId, String lon, String lat,
                                         String desc, String tags, String name,String deviceID,String uniqueAreadId,String uniqueId) {
        JSONObject postParams = new JSONObject();
        try {
            if(id!=null){
                postParams.put("id",id);
            }
            if(deviceID==null){
                deviceID = getDeviceId();
            }
            postParams.put("requestType", "PositionMaster");
            postParams.put("queryType", queryType);
            postParams.put("deviceID",deviceID);
            postParams.put("area_id",areaId);
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

    public String getDeviceId(){
        String deviceID = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            deviceID = (String) (get.invoke(c, "ro.serialno", "unknown"));
            return deviceID;
        }catch (Exception ignored){
            ignored.printStackTrace();
        }
        return deviceID;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, POSITION_TABLE_NAME);
        return numRows;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + POSITION_TABLE_NAME + " where " + POSITION_COLUMN_ID + "=" + id + "", null);
        return res;
    }

    public String getUniqueId(int id){
        Cursor data = getData(id);
        data.moveToFirst();
        return data.getString(data.getColumnIndex(POSITION_COLUMN_UNIQUE_ID));
    }

    public boolean updatePosition(Integer id, Integer areaId, String name, String desc, String lat, String lon, String tags,
                                  String uniqueAreadId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String uniqueId = getUniqueId(id);
        ContentValues contentValues = new ContentValues();
        contentValues.put(POSITION_COLUMN_AREA_ID, areaId);
        contentValues.put(POSITION_COLUMN_NAME, name);
        contentValues.put(POSITION_COLUMN_DESCRIPTION, desc);
        contentValues.put(POSITION_COLUMN_LAT, lat);
        contentValues.put(POSITION_COLUMN_LON, lon);
        contentValues.put(POSITION_COLUMN_TAGS, tags);
        db.update(POSITION_TABLE_NAME, contentValues, POSITION_COLUMN_ID + " = ? ", new String[]{Integer.toString(id)});
        JSONObject postParams = preparePostParams("update",id,areaId,name,lon,lat,desc,name,getDeviceId(),uniqueAreadId,uniqueId);
        new LandMapAsyncRestSync().execute(postParams);
        return true;
    }

    public Integer deletePosition(Long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String uniqueId = getUniqueId(id.intValue());
        int delete = db.delete(POSITION_TABLE_NAME,
                POSITION_COLUMN_ID + " = ? ",
                new String[]{id + ""});
        JSONObject postParams = preparePostParams("delete",uniqueId);
        new LandMapAsyncRestSync().execute(postParams);
        return delete;
    }

    public void deletePositionByName(String pName, String uniqueAreaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+POSITION_TABLE_NAME+" WHERE "
                + POSITION_COLUMN_UNIQUE_AREA_ID +" = "+uniqueAreaId
                + " AND "+POSITION_COLUMN_NAME+" = '"+pName+"'");
        JSONObject postParams = preparePostParams("deleteByName",uniqueAreaId,pName);
        new LandMapAsyncRestSync().execute(postParams);
    }

    public void deletePositionByUniqueId(String uniqueAreaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+POSITION_TABLE_NAME+" WHERE "
                + POSITION_COLUMN_UNIQUE_AREA_ID +" = '"+uniqueAreaId+"'");
        JSONObject postParams = preparePostParams("deleteByUniqueAreaId", uniqueAreaId, null);
        new LandMapAsyncRestSync().execute(postParams);
    }

    public void deleteAllPositions(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ POSITION_TABLE_NAME);
    }

    public ArrayList<String> getAllPositions() {
        ArrayList<String> positions = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + POSITION_TABLE_NAME, null);
        cursor.moveToFirst();

        while (cursor.isAfterLast() == false) {
            positions.add(cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_NAME)));
            cursor.moveToNext();
        }
        return positions;
    }

    public ArrayList<PositionElement> getAllPositionForArea(AreaElement ae) {
        ArrayList<PositionElement> pes = new ArrayList<PositionElement>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + POSITION_TABLE_NAME + " WHERE " + POSITION_COLUMN_AREA_ID + "=?",
                new String[]{ae.getId() + ""});
        if(cursor != null){
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                PositionElement pe = new PositionElement();
                pe.setId(new Long(cursor.getInt(cursor.getColumnIndex(POSITION_COLUMN_ID))));

                pe.setName(cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_NAME)));
                pe.setDescription(cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_DESCRIPTION)));

                String latStr = cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_LAT));
                pe.setLat(Double.parseDouble(latStr));

                String lonStr = cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_LON));
                pe.setLon(Double.parseDouble(lonStr));

                pe.setAreaId(ae.getId());
                pe.setTags(cursor.getString(cursor.getColumnIndex(POSITION_COLUMN_TAGS)));
                pes.add(pe);

                cursor.moveToNext();
            }
        }
        return pes;
    }
}