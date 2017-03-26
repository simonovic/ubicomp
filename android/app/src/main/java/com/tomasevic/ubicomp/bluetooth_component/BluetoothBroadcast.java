package com.tomasevic.ubicomp.bluetooth_component;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.ArrayList;

/**
 * Created by Tomasevic on 18.10.2016..
 */
public class BluetoothBroadcast
{
    BroadcastReceiver mReceiver;

    public BluetoothBroadcast(ArrayList<String> devices)
    {
        mReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                if(BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                }
            }
        };
    }
}
