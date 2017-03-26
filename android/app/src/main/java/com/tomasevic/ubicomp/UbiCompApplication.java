package com.tomasevic.ubicomp;

import android.app.Application;
import android.content.Context;

/**
 * Created by Tomasevic on 9.11.2016..
 */
public class UbiCompApplication extends Application
{
    public static UbiCompApplication instance;

    public UbiCompApplication()
    {
        instance = this;
    }

    public static Context getContext()
    {
        return instance;
    }
}
