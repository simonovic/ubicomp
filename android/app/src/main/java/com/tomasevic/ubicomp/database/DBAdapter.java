package com.tomasevic.ubicomp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.tomasevic.ubicomp.models.DataModel;

import java.util.ArrayList;

/**
 * Created by Tomasevic on 8.11.2016..
 */
public class DBAdapter
{
    public static final String DB_NAME = "ubicomp";
    public static final String DB_TABLE = "sensor_data";
    public static final int DB_VERSION = 1;

    public static final String ID = "id";
    public static final String TEMPERATURE = "temperature";
    public static final String HUMIDITY = "humidity";
    public static final String AIR_QUALITY = "air_quality";
    public static final String HEAT_INDEX = "heat_index";
    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final String TIMESTAMP = "timestamp";

    private SQLiteDatabase db;
    private final Context context;
    private DBHelper helper;

    public DBAdapter(Context cont)
    {
        context = cont;
        helper = new DBHelper(context, DB_NAME, null, DB_VERSION);
    }

    public DBAdapter open() throws SQLiteException
    {
        db = helper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        db.close();
    }

    public long insertRecord(DataModel record)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TEMPERATURE, record.getTemperature());
        contentValues.put(HUMIDITY, record.getHumidity());
        contentValues.put(AIR_QUALITY, record.getAir_quality());
        contentValues.put(HEAT_INDEX, record.getHeat_index());
        contentValues.put(LAT, record.getLat());
        contentValues.put(LNG, record.getLng());
        contentValues.put(TIMESTAMP, record.getTimestamp());

        long id = -1;
        db.beginTransaction();
        try
        {
            id = db.insert(DB_TABLE, null, contentValues);
            db.setTransactionSuccessful();
        }
        catch (SQLiteException e) { Log.w("DBAdapter", e.getMessage()); }
        finally { db.endTransaction(); }

        return id;
    }

    public boolean deleteRecord()
    {
        return true;
    }

    public boolean clearDatabase()
    {
        //db.beginTransaction();
        try
        {
            db.delete(DB_TABLE, "id > 0", null);
        }
        catch (SQLiteException e)
        {
            Log.w("DBAdapter", e.getMessage());
            return false;
        }
        finally {
           // db.endTransaction();
        }
        ArrayList<DataModel> tmp = getAllRecords();
        return true;
    }

    public DataModel getRecord(long id)
    {
        DataModel model = null;
        Cursor cursor = null;
        db.beginTransaction();

        try
        {
            cursor = db.query(DB_TABLE, null, ID + "=" + id, null, null, null, null);
            db.setTransactionSuccessful();
        }
        catch (SQLiteException e) { Log.w("DBAdapter", e.getMessage()); }
        finally { db.endTransaction(); }

        if(cursor != null)
        {
            while(cursor.moveToFirst())
            {
                int timestamp = cursor.getInt(cursor.getColumnIndex(TIMESTAMP));
                model = new DataModel(timestamp);
                model.setLat(cursor.getDouble(cursor.getColumnIndex(LAT)));
                model.setLng(cursor.getDouble(cursor.getColumnIndex(LNG)));
                model.setTemperature(cursor.getInt(cursor.getColumnIndex(TEMPERATURE)));
                model.setHumidity(cursor.getInt(cursor.getColumnIndex(HUMIDITY)));
                model.setHeat_index(cursor.getFloat(cursor.getColumnIndex(HEAT_INDEX)));
                model.setAir_quality(cursor.getInt(cursor.getColumnIndex(AIR_QUALITY)));
            }
        }
        return model;
    }

    public ArrayList<DataModel> getAllRecords()
    {
        ArrayList<DataModel> records = new ArrayList<>();
        Cursor cursor = null;
        db.beginTransaction();

        try
        {
            cursor = db.query(DB_TABLE, null, null, null, null, null, null);
            db.setTransactionSuccessful();
        }
        catch (SQLiteException e) { Log.w("DBAdapter", e.getMessage()); }
        finally { db.endTransaction(); }

        if(cursor != null)
        {
            DataModel model = null;

            while (cursor.moveToNext())
            {
                int timestamp = cursor.getInt(cursor.getColumnIndex(TIMESTAMP));
                model = new DataModel(timestamp);
                model.setLat(cursor.getDouble(cursor.getColumnIndex(LAT)));
                model.setLng(cursor.getDouble(cursor.getColumnIndex(LNG)));
                model.setTemperature(cursor.getInt(cursor.getColumnIndex(TEMPERATURE)));
                model.setHumidity(cursor.getInt(cursor.getColumnIndex(HUMIDITY)));
                model.setHeat_index(cursor.getFloat(cursor.getColumnIndex(HEAT_INDEX)));
                model.setAir_quality(cursor.getInt(cursor.getColumnIndex(AIR_QUALITY)));

                records.add(model);
            }
        }
        return records;
    }
}
























