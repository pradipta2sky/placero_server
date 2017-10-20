package lm.pkp.com.landmap;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
                        POSITION_COLUMN_TAGS + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + POSITION_TABLE_NAME);
        onCreate(db);
    }

    public PositionElement insertPosition(Integer areaId, String name, String desc, String lat, String lon, String tags) {
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

        long id = db.insert(POSITION_TABLE_NAME, null, contentValues);
        pe.setId(id);

        return pe;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, POSITION_TABLE_NAME);
        return numRows;
    }

    public boolean updatePosition(Integer id, Integer areaId, String name, String desc, String lat, String lon, String tags) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(POSITION_COLUMN_AREA_ID, areaId);
        contentValues.put(POSITION_COLUMN_NAME, name);
        contentValues.put(POSITION_COLUMN_DESCRIPTION, desc);
        contentValues.put(POSITION_COLUMN_LAT, lat);
        contentValues.put(POSITION_COLUMN_LON, lon);
        contentValues.put(POSITION_COLUMN_TAGS, tags);
        db.update(POSITION_TABLE_NAME, contentValues, POSITION_COLUMN_ID + " = ? ", new String[]{Integer.toString(id)});
        return true;
    }

    public Integer deletePosition(Long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(POSITION_TABLE_NAME,
                POSITION_COLUMN_ID + " = ? ",
                new String[]{id + ""});
    }

    public void deletePositionByName(String pName, Integer areaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+POSITION_TABLE_NAME+" WHERE "
                + POSITION_COLUMN_AREA_ID +" = "+areaId
                + " AND "+POSITION_COLUMN_NAME+" = '"+pName+"'");
    }

    public void deleteAllPositions(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ POSITION_TABLE_NAME);
    }

    public ArrayList<String> getAllPositions() {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + POSITION_TABLE_NAME, null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            array_list.add(res.getString(res.getColumnIndex(POSITION_COLUMN_NAME)));
            res.moveToNext();
        }
        return array_list;
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