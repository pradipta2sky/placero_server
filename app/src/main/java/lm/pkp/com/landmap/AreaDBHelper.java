package lm.pkp.com.landmap;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class AreaDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    private Context localContext = null;

    public static final String AREA_TABLE_NAME = "area_master";
    public static final String AREA_COLUMN_ID = "id";
    public static final String AREA_COLUMN_NAME = "name";
    public static final String AREA_COLUMN_DESCRIPTION = "desc";
    public static final String AREA_COLUMN_CENTER_LAT = "center_lat";
    public static final String AREA_COLUMN_CENTER_LON = "center_lon";

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
                        AREA_COLUMN_CENTER_LAT + " text, " +
                        AREA_COLUMN_CENTER_LON + " text)"
        );
        new PositionsDBHelper(localContext).onCreate(db);
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
        db.insert(AREA_TABLE_NAME, null, contentValues);
        return true;
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

    public boolean updateArea(Integer id, String name, String desc, String centerLat, String centerLon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AREA_COLUMN_NAME, name);
        contentValues.put(AREA_COLUMN_DESCRIPTION, desc);
        contentValues.put(AREA_COLUMN_CENTER_LAT, centerLat);
        contentValues.put(AREA_COLUMN_CENTER_LON, centerLon);
        db.update(AREA_TABLE_NAME, contentValues, AREA_COLUMN_ID + " = ? ", new String[]{Integer.toString(id)});
        return true;
    }

    public Integer deleteArea(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();

        PositionsDBHelper pdb = new PositionsDBHelper(localContext);
        pdb.deletePositionByAreaId(id);

        return db.delete(AREA_TABLE_NAME,
                AREA_COLUMN_ID + " = ? ",
                new String[]{Integer.toString(id)});
    }

    public void deleteAllAreas(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ AREA_TABLE_NAME);
    }

    public ArrayList<AreaElement> getAllAreas() {
        ArrayList<AreaElement> allAreas = new ArrayList<AreaElement>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + AREA_TABLE_NAME, null);
            if(cursor == null){
                return allAreas;
            }
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (cursor.isAfterLast() == false) {
                    AreaElement ae = new AreaElement();
                    ae.setId(cursor.getInt(cursor.getColumnIndex(AREA_COLUMN_ID)));
                    ae.setName(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_NAME)));
                    ae.setDescription(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_DESCRIPTION)));
                    allAreas.add(ae);

                    cursor.moveToNext();
                }
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return allAreas;
    }

    public AreaElement getAreaByName(String name){
        Cursor cursor = null;
        AreaElement ae = new AreaElement();
        try {
            cursor = this.getReadableDatabase().rawQuery("SELECT * FROM "+AREA_TABLE_NAME+" WHERE "+AREA_COLUMN_NAME+"=?",
                    new String[] {name});
            if(cursor == null){
                return ae;
            }
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                ae.setId(cursor.getInt(cursor.getColumnIndex(AREA_COLUMN_ID)));
                ae.setName(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_NAME)));
                ae.setDescription(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_DESCRIPTION)));

                PositionsDBHelper pdb = new PositionsDBHelper(localContext);
                ae.setPositions(pdb.getAllPositionForArea(ae));
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return ae;
    }

    public AreaElement getAvailableArea(){
        Cursor cursor = null;
        AreaElement ae = new AreaElement();
        try {
            cursor = this.getReadableDatabase().rawQuery("SELECT * FROM "+AREA_TABLE_NAME, new String[]{});
            if(cursor == null){
                return ae;
            }
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                ae.setId(cursor.getInt(cursor.getColumnIndex(AREA_COLUMN_ID)));
                ae.setName(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_NAME)));
                ae.setDescription(cursor.getString(cursor.getColumnIndex(AREA_COLUMN_DESCRIPTION)));

                PositionsDBHelper pdb = new PositionsDBHelper(localContext);
                ae.setPositions(pdb.getAllPositionForArea(ae));
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return ae;
    }

}