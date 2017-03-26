package com.tomasevic.ubicomp.bluetooth_component;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tomasevic.ubicomp.Constants;
import com.tomasevic.ubicomp.R;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity implements View.OnClickListener
{
    BluetoothAdapter blAdapter;
    int REQUEST_ENABLE_BT = 0;
    ListView pairedDevicesLV;
    ArrayList<String> pairedDevicesList;
    ArrayAdapter<String> listAdapter;
    CoordinatorLayout coordinatorLayout;
    FloatingActionButton fab;
    SwitchCompat switchButton;
    Set<BluetoothDevice> pairedDevices;
    TextView bluetooth_off;
    LinearLayout linLayoutPairedDevices;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                pairedDevicesList.add(device.getName() + "\n" + device.getAddress());
                listAdapter.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                showToast("Bluetooth discovery finished!");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.bluetooth_action_bar);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);

        bluetooth_off = (TextView)findViewById(R.id.bluetooth_off);
        switchButton = (SwitchCompat)findViewById(R.id.switchForActionBar);
        fab = (FloatingActionButton) findViewById(R.id.refresh_devices_list);
        linLayoutPairedDevices = (LinearLayout) findViewById(R.id.linLayoutPairedDevices);

        pairedDevicesLV = (ListView)findViewById(R.id.paired_devices);
        pairedDevicesList = new ArrayList<>();

        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.bluetoothCoordinataorLayout);

        listAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, pairedDevicesList);
        pairedDevicesLV.setAdapter(listAdapter);
        pairedDevicesLV.setOnItemClickListener(onPairedDevicesClickListener);

        blAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        if(blAdapter == null) {
            Log.d("Bluetooth", "Device does not support bluetooth");
        } else if (blAdapter.isEnabled()) {
            switchButton.setChecked(true);
            bluetoothIsActive(true);
            getSetOfDevices();
        }

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    activateBluetooth();
                    bluetoothIsActive(true);
                } else {
                    blAdapter.disable();
                    pairedDevicesList.clear();
                    listAdapter.notifyDataSetChanged();
                    bluetoothIsActive(false);
                }
            }

        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Refreshing list of devices", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if (blAdapter.isDiscovering()) {
                    blAdapter.cancelDiscovery();
                }
                blAdapter.startDiscovery();
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == RESULT_OK)
            getSetOfDevices();
        else if(resultCode == RESULT_CANCELED)
        {
            switchButton.setChecked(false);
        }
    }

    public void activateBluetooth() {
        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
    }

    public void getSetOfDevices()
    {
        pairedDevices = blAdapter.getBondedDevices();
        pairedDevicesList.clear();
        if(pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesList.add(device.getName() + "\n" + device.getAddress());
            }
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

    private AdapterView.OnItemClickListener onPairedDevicesClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String[] blNameAndAddress = pairedDevicesList.get(position).split("\n");
            if (blNameAndAddress[0].equals(Constants.ARDUINO_BLUETOOTH_NAME)) {
                Intent intent = new Intent(BluetoothActivity.this, CommunicationWithArduinoActivity.class);
                intent.putExtra(Constants.EXTRA_BL_ADDRESS, blNameAndAddress[1]);
                startActivity(intent);
            }
        }
    };

    public void bluetoothIsActive(boolean active) {
        if(active) {
            bluetooth_off.setVisibility(View.GONE);
            linLayoutPairedDevices.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
        }
        else {
            fab.setVisibility(View.GONE);
            linLayoutPairedDevices.setVisibility(View.GONE);
            bluetooth_off.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
