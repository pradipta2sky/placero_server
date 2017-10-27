package lm.pkp.com.landmap.area;


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

import lm.pkp.com.landmap.sync.LandMapAsyncRestSync;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.util.AndroidSystemUtil;

public class AreaDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "landmap.db";
    private Context localContext = null;
    private final LandMapAsyncRestSync syncher = new LandMapAsyncRestSync();

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

    public boolean insertArea(String name, String desc) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(AREA_COLUMN_NAME, name);
        contentValues.put(AREA_COLUMN_DESCRIPTION, desc);
        String createdBy = UserContext.getInstance().getUserElement().getEmail();
        contentValues.put(AREA_COLUMN_CREATED_BY, createdBy);
        contentValues.put(AREA_COLUMN_OWNERSHIP_TYPE, "self");
        contentValues.put(AREA_COLUMN_CURRENT_OWNER, createdBy);

        String uniqueId = UUID.randomUUID().toString();
        contentValues.put(AREA_COLUMN_UNIQUE_ID, uniqueId);
        contentValues.put(AREA_COLUMN_TAGS, "");
        contentValues.put(AREA_COLUMN_CENTER_LAT, "0.0");
        contentValues.put(AREA_COLUMN_CENTER_LON, "0.0");
        contentValues.put(AREA_COLUMN_MEASURE_SQ_FT, "0");

        db.insert(AREA_TABLE_NAME, null, contentValues);

        JSONObject postParams = preparePostParams("insert", uniqueId, "0.0", "0.0", desc, name,
                AndroidSystemUtil.getDeviceId(),"self", "0", createdBy, createdBy);
        syncher.execute(postParams);

        db.close();
        return true;
    }

    public void insertAreaToServer(AreaElement ae){
        JSONObject postParams = preparePostParams("insert",ae.getUniqueId(), ae.getCenterLon() + "",
                ae.getCenterLat() + "", ae.getDescription(), ae.getName(), AndroidSystemUtil.getDeviceId(),
                ae.getOwnershipType(), ae.getMeasureSqFt() + "", ae.getCreatedBy(), ae.getCurrentOwner());

        syncher.execute(postParams);
    }

    public AreaElement insertAreaFromServer(AreaElement ae) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(AREA_COLUMN_UNIQUE_ID, ae.getUniqueId());
        contentValues.put(AREA_COLUMN_NAME, ae.getName());
        contentValues.put(AREA_COLUMN_DESCRIPTION, ae.getDescription());
        contentValues.put(AREA_COLUMN_CREATED_BY, ae.getCreatedBy());
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

        JSONObject postParams = preparePostParams("update", ae.getUniqueId(), ae.getCenterLon() + "",
                ae.getCenterLat() + "", ae.getDescription(), ae.getName(), AndroidSystemUtil.getDeviceId(),
                ae.getOwnershipType(), ae.getMeasureSqFt() + "", ae.getCreatedBy(), ae.getCurrentOwner());

        syncher.execute(postParams);
        db.close();
        return true;
    }

    public Integer deleteArea(AreaElement ae) {
        SQLiteDatabase db = this.getWritableDatabase();

        PositionsDBHelper pdb = new PositionsDBHelper(localContext);
        pdb.deletePositionByUniqueId(ae.getUniqueId());
        int delete = db.delete(AREA_TABLE_NAME,
                AREA_COLUMN_UNIQUE_ID + " = ? ",
                new String[]{ae.getUniqueId()});
        JSONObject postParams = preparePostParams("delete", ae.getUniqueId());
        syncher.execute(postParams);

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

    private JSONObject preparePostParams(String queryType, String uniqueId) {
        JSONObject postParams = new JSONObject();
        postParams = preparePostParams(queryType, uniqueId, null, null, null, null, null, null, null, null, null);
        new LandMapAsyncRestSync().execute(postParams);
        return postParams;
    }

    private JSONObject preparePostParams(String queryType,String uid, String cln, String clt, String desc, String name,
                                         String did, String owt, String mSqFt, String cBy, String cown) {
        JSONObject postParams = new JSONObject();
        try {
            if (did == null) {
                did = AndroidSystemUtil.getDeviceId();
            }

            postParams.put("requestType", "AreaMaster");
            postParams.put("queryType", queryType);
            postParams.put("deviceID", did);
            postParams.put("center_lon", cln);
            postParams.put("center_lat", clt);
            postParams.put("desc", desc);
            postParams.put("name", name);
            postParams.put("created_by", cBy);
            postParams.put("unique_id", uid);
            postParams.put("own_type", owt);
            postParams.put("msqft", mSqFt);
            postParams.put("cown", cown);

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