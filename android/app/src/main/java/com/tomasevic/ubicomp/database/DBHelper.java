package com.tomasevic.ubicomp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Tomasevic on 8.11.2016..
 */
public class DBHelper extends SQLiteOpenHelper
{
    public static final String DB_CREATE = "create table " + DBAdapter.DB_TABLE + " ("
            + DBAdapter.ID + " INTEGER primary key autoincrement, "
            + DBAdapter.TIMESTAMP + " INTEGER, "
            + DBAdapter.LAT + " DOUBLE, "
            + DBAdapter.LNG + " DOUBLE, "
            + DBAdapter.TEMPERATURE + " INTEGER, "
            + DBAdapter.HUMIDITY + " INTEGER, "
            + DBAdapter.HEAT_INDEX + " FLOAT, "
            + DBAdapter.AIR_QUALITY + " INTEGER);";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        try
        {
            db.execSQL(DB_CREATE);
        }
        catch (SQLiteException e)
        {
            Log.w("DBHelper", e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS" + DBAdapter.DB_TABLE);
    }
}
