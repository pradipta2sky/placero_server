package lm.pkp.com.landmap.weather.db;


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
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.sync.LMSRestAsyncTask;
import lm.pkp.com.landmap.util.AndroidSystemUtil;
import lm.pkp.com.landmap.weather.model.WeatherElement;

public class WeatherDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "landmap.db";
    private static final String TABLE_NAME = "weather_master";

    private static final String WEATHER_COLUMN_UNIQUE_ID = "unique_id";
    private static final String WEATHER_POSITION_COLUMN_ID = "position_id";
    private static final String WEATHER_COLUMN_TEMPERATURE = "temperature";
    private static final String WEATHER_COLUMN_CONDITION_CODE = "condition_c";
    private static final String WEATHER_COLUMN_CONDITION_TEXT = "condition_t";
    private static final String WEATHER_COLUMN_ADDRESS = "address";
    private static final String WEATHER_COLUMN_WIND_CHILL = "wind_chill";
    private static final String WEATHER_COLUMN_WIND_DIRECTION = "wind_direction";
    private static final String WEATHER_COLUMN_WIND_SPEED = "wind_speed";
    private static final String WEATHER_COLUMN_HUMIDITY = "humidity";
    private static final String WEATHER_COLUMN_VISIBILITY = "visibility";
    private static final String WEATHER_COLUMN_CREATED_MILLIS = "created_on";

    public WeatherDBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + TABLE_NAME + "(" +
                        WEATHER_COLUMN_UNIQUE_ID + " text," +
                        WEATHER_POSITION_COLUMN_ID + " text," +
                        WEATHER_COLUMN_TEMPERATURE + " text," +
                        WEATHER_COLUMN_CONDITION_CODE + " text, " +
                        WEATHER_COLUMN_CONDITION_TEXT + " text, " +
                        WEATHER_COLUMN_ADDRESS + " text," +
                        WEATHER_COLUMN_WIND_CHILL + " text," +
                        WEATHER_COLUMN_WIND_DIRECTION + " text," +
                        WEATHER_COLUMN_WIND_SPEED + " text," +
                        WEATHER_COLUMN_HUMIDITY + " text," +
                        WEATHER_COLUMN_VISIBILITY + " text," +
                        WEATHER_COLUMN_CREATED_MILLIS + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertWeatherLocally(WeatherElement we) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(WEATHER_COLUMN_UNIQUE_ID, we.getUniqueId());
        contentValues.put(WEATHER_POSITION_COLUMN_ID, we.getPositionId());
        contentValues.put(WEATHER_COLUMN_TEMPERATURE, we.getTemperature());
        contentValues.put(WEATHER_COLUMN_CONDITION_CODE, we.getConditionCode());
        contentValues.put(WEATHER_COLUMN_CONDITION_TEXT, we.getConditionText());
        contentValues.put(WEATHER_COLUMN_ADDRESS, we.getAddress());
        contentValues.put(WEATHER_COLUMN_WIND_CHILL, we.getWindChill());
        contentValues.put(WEATHER_COLUMN_WIND_DIRECTION, we.getWindDirection());
        contentValues.put(WEATHER_COLUMN_WIND_SPEED, we.getWindSpeed());
        contentValues.put(WEATHER_COLUMN_HUMIDITY, we.getHumidity());
        contentValues.put(WEATHER_COLUMN_VISIBILITY, we.getVisibility());
        contentValues.put(WEATHER_COLUMN_CREATED_MILLIS, we.getCreatedOn());

        db.insert(TABLE_NAME, null, contentValues);
        db.close();
    }

    public void deleteWeatherElement(WeatherElement we) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, WEATHER_COLUMN_UNIQUE_ID + " = ? ", new String[]{we.getUniqueId()});
        db.close();
    }

    public void deleteWeatherByPosition(PositionElement pe) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE "
                + WEATHER_POSITION_COLUMN_ID + " = '" + pe.getUniqueId() + "'");
        db.close();
    }

    public WeatherElement getWeatherByPosition(PositionElement pe) {
        WeatherElement we = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME + " WHERE " + WEATHER_POSITION_COLUMN_ID + "=?",
                new String[]{pe.getUniqueId()});
        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                we = new WeatherElement();
                we.setUniqueId(cursor.getString(cursor.getColumnIndex(WEATHER_COLUMN_UNIQUE_ID)));
                we.setAddress(cursor.getString(cursor.getColumnIndex(WEATHER_COLUMN_ADDRESS)));
                we.setConditionCode(cursor.getString(cursor.getColumnIndex(WEATHER_COLUMN_CONDITION_CODE)));
                we.setConditionText(cursor.getString(cursor.getColumnIndex(WEATHER_COLUMN_CONDITION_TEXT)));
                we.setHumidity(cursor.getString(cursor.getColumnIndex(WEATHER_COLUMN_HUMIDITY)));
                we.setTemperature(cursor.getString(cursor.getColumnIndex(WEATHER_COLUMN_TEMPERATURE)));
                we.setVisibility(cursor.getString(cursor.getColumnIndex(WEATHER_COLUMN_VISIBILITY)));
                we.setWindChill(cursor.getString(cursor.getColumnIndex(WEATHER_COLUMN_WIND_CHILL)));
                we.setWindDirection(cursor.getString(cursor.getColumnIndex(WEATHER_COLUMN_WIND_DIRECTION)));
                we.setWindSpeed(cursor.getString(cursor.getColumnIndex(WEATHER_COLUMN_WIND_SPEED)));
                we.setCreatedOn(cursor.getString(cursor.getColumnIndex(WEATHER_COLUMN_CREATED_MILLIS)));
                we.setPositionId(pe.getUniqueId());
                break;
            }
            cursor.close();
        }
        db.close();
        return we;
    }


    public void deleteWeatherElementsLocally() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "1", null);
        db.close();
    }

}