package com.tomasevic.ubicomp.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.tomasevic.ubicomp.UbiCompApplication;
import com.tomasevic.ubicomp.database.DBDataManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Tomasevic on 10.11.2016..
 */
public class DatabaseSyncService extends Service
{
    private static Timer timer = new Timer();
    private Context context;
    private int cycle;
    public static int interval = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void onCreate()
    {
        super.onCreate();
        context = this;
        cycle = 0;
        startService();
    }

    public void onDestroy()
    {
        super.onDestroy();
        Toast.makeText(this, "Service stopped ...", Toast.LENGTH_LONG).show();
    }

    private void startService()
    {
        Toast.makeText(getApplicationContext(), "Service started", Toast.LENGTH_SHORT).show();
        timer.scheduleAtFixedRate(new MainTask(), 1, 60000 * interval);
    }

    private class MainTask extends TimerTask
    {

        @Override
        public void run()
        {
            toastHandler.sendEmptyMessage(0);
        }
    }

    private final Handler toastHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(cycle > 0)
            {
                DBDataManager.getInstance().syncData();
            }
            cycle++;
        }
    };

    public static int getInterval()
    {
        return interval;
    }

    public static void setInterval(int intvl)
    {
        interval = intvl;
        Log.w("new interval value", Integer.toString(interval));
    }
}
