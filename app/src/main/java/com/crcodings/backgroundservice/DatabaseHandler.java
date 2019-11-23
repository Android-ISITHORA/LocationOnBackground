package com.crcodings.backgroundservice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "LocationManager.db";
    private static final String TABLE_LOCATION = "locations";

    private static final String COLUMN_LOC_ID = "column_loc_id";
    private static final String COLUMN_LATITUDE = "column_latitude";
    private static final String COLUMN_LONGITUDE = "column_longitude";

    private SQLiteDatabase db;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String CREATE_USER_TABLE_LOCATION = "CREATE TABLE " +
                TABLE_LOCATION + "(" +
                COLUMN_LOC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_LATITUDE + " TEXT," +
                COLUMN_LONGITUDE + " TEXT" + ")";

        sqLiteDatabase.execSQL(CREATE_USER_TABLE_LOCATION);

    }

    public void insertLatLang(LocationModel location) {

        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, location.getLatitude());
        values.put(COLUMN_LONGITUDE, location.getLongitude());
        db.insert(TABLE_LOCATION, null, values);
        db.close();
    }

    public ArrayList<LocationModel> getLocationData() {
        String[] columns = {
                COLUMN_LOC_ID,
                COLUMN_LATITUDE,
                COLUMN_LONGITUDE


        };
        String sortOrder =
                COLUMN_LOC_ID + " ASC";
        ArrayList<LocationModel> locationModelList = new ArrayList<>();

        db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_LOCATION, columns, null, null, null, null, sortOrder);


        if (cursor.moveToFirst()) {
            do {

                String id = cursor.getString(0);
                String latitude = cursor.getString(1);
                String longitude = cursor.getString(2);

                LocationModel locationModel = new LocationModel();
                locationModel.setLoc_Id(id);
                locationModel.setLatitude(latitude);
                locationModel.setLongitude(longitude);
                locationModelList.add(locationModel);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return locationModelList;
    }

    public void deleteDate()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DatabaseHandler.TABLE_LOCATION, null, null);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
