package com.tomasevic.ubicomp.bluetooth_component;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.tomasevic.ubicomp.Constants;
import com.tomasevic.ubicomp.R;
import com.tomasevic.ubicomp.database.DBAdapter;
import com.tomasevic.ubicomp.database.DBDataManager;
import com.tomasevic.ubicomp.models.DataArrayModel;
import com.tomasevic.ubicomp.models.DataModel;
import com.tomasevic.ubicomp.services.DatabaseSyncService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class CommunicationWithArduinoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_CODE = 100;

    String macAddress;
    BluetoothAdapter btAdapter;
    BluetoothSocket btSocket;
    boolean connect = true;
    ProgressDialog progress;
    ConnectingToArduinoAsyncTask connectingToArduinoAsyncTask;
    ReceivingDataFromArduinoAsyncTask receivingDataFromArduinoAsyncTask;
    String receivedData;
    ArrayList<String> dataList;
    ArrayAdapter<String> dataAdapter;
    LocationManager locationManager;
    LocationListener locationListener;
    double lat;
    double lng;
    Double startLat;
    Double startLng;
    boolean connectionButtonEnabled;

    Button btnConnect;
    Button btnSendData;
    ListView listViewData;
    RadioGroup radioGroup;
    int samplingDistance = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_with_arduino);

        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnSendData = (Button) findViewById(R.id.btnSendData);
        listViewData = (ListView) findViewById(R.id.listViewData);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        macAddress = getIntent().getStringExtra(Constants.EXTRA_BL_ADDRESS);
        receivedData = "";
        dataList = new ArrayList<>();
        dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, dataList);
        listViewData.setAdapter(dataAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                if(startLat == null && startLng == null)
                {
                    startLat = location.getLatitude();
                    startLng = location.getLongitude();
                }
                else if(!connect)
                {
                    double currentLat = location.getLatitude();
                    double currentLng = location.getLongitude();
                    float [] results = new float[1];
                    Location.distanceBetween(startLat, startLng, currentLat, currentLng, results);
                    Log.w("location change", String.valueOf(results[0]));
                    if(results[0] >= samplingDistance)
                    {
                        startLat = currentLat;
                        startLng = currentLng;
                        Toast.makeText(getApplicationContext(), "distance is grater than " + samplingDistance + "m", Toast.LENGTH_LONG).show();
                        setArduinoMode(3);
                    }

                }
                lat = location.getLatitude();
                lng = location.getLongitude();

                if (!connectionButtonEnabled) {
                    btnConnect.setEnabled(true);
                    connectionButtonEnabled = true;
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_CODE);
                return;
            } else {
                setRequestLocationUpdates();
            }
        } else {
            setRequestLocationUpdates();
        }

        btnConnect.setOnClickListener(this);
        btnSendData.setOnClickListener(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(!connect) {
            connectToArduino();
        }
        if (progress != null) {
            progress.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setRequestLocationUpdates();
                }
                break;
        }
    }

    private void setRequestLocationUpdates() {
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            lat = lastKnownLocation.getLatitude();
            lng = lastKnownLocation.getLongitude();
            btnConnect.setEnabled(true);
            connectionButtonEnabled = true;
        }
        locationManager.requestLocationUpdates("gps", 3000, 0, locationListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConnect:
                connectToArduino();
                break;
            case R.id.btnSendData:
                setArduinoMode(radioGroup.getCheckedRadioButtonId());
                break;
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void connectToArduino() {
        if (connect) {
            connectingToArduinoAsyncTask = new ConnectingToArduinoAsyncTask();
            connectingToArduinoAsyncTask.execute();
        } else if (btSocket != null) {
            try {
                connect = true;
                btSocket.close();
            } catch (IOException e) {
                connect = false;
                showToast("Error disconnecting from Arduino!");
                System.out.println("Error disconnecting from Arduino!");
            }
            if (connect) {
                btnConnect.setText(getString(R.string.connect));
                receivingDataFromArduinoAsyncTask.cancel(true);
            }
        }
    }

    private void setArduinoMode(int id) {

        if (btSocket != null && id != -1) {
            try {
                btSocket.getOutputStream().write((byte) id%3 == 0 ? 3 : id%3);
            } catch (IOException e) {
                showToast("Error sending data to Arduino!");
                System.out.println("Error sending data to Arduino!");
            }
        }
    }

    private class ConnectingToArduinoAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(CommunicationWithArduinoActivity.this, "Connecting...", "Please wait!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                connect = false;
                btAdapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice arduino = btAdapter.getRemoteDevice(macAddress);
                btSocket = arduino.createInsecureRfcommSocketToServiceRecord(Constants.uuid);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                btSocket.connect();
            } catch (IOException e) {
                connect = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (connect) {
                showToast("Connecting failed!");
                System.out.println("Connecting failed!");
            } else {
                btnConnect.setText(getString(R.string.disconnect));
                receivingDataFromArduinoAsyncTask = new ReceivingDataFromArduinoAsyncTask();
                receivingDataFromArduinoAsyncTask.execute();
//                startService(new Intent(getApplicationContext(), DatabaseSyncService.class));
            }
            progress.dismiss();
        }
    }

    private class ReceivingDataFromArduinoAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected Void doInBackground(Void... devices) {
            InputStream inputStream = null;
            try {
                inputStream = btSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] buffer = new byte[100];
            int bytes;

            if (inputStream != null) {
                while (true) {
                    try {
                        bytes = inputStream.read(buffer);
                        final String readMessage = new String(buffer, 0, bytes);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onReceiveData(readMessage);
                            }
                        });
                    } catch (IOException e) {
                        System.out.println("Error receiving data from Arduino!");
                        break;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    private void onReceiveData(String part) {
        receivedData += part;
        if (part.contains("#")) {
            receivedData = receivedData.substring(0, receivedData.indexOf("#"));
            System.out.println(receivedData);
            dataList.add(receivedData);
            dataAdapter.notifyDataSetChanged();
            sendSensorData(receivedData);
            receivedData = "";
        }
    }

    private void sendSensorData(String receivedData) {
        String[] parsedData = receivedData.split(" ");
        DataModel model = new DataModel(System.currentTimeMillis()/1000);
        model.setHumidity(Integer.parseInt(parsedData[1].substring(0, parsedData[1].indexOf(","))));
        model.setTemperature(Integer.parseInt(parsedData[3].substring(0, parsedData[3].indexOf(","))));
        model.setHeat_index(Float.parseFloat(parsedData[5].substring(0, parsedData[5].indexOf(","))));
        model.setAir_quality(Integer.parseInt(parsedData[7].substring(0, parsedData[7].indexOf(","))));
        model.setLat(lat);
        model.setLng(lng);

        //local db
        DBDataManager.getInstance().addNewRecord(model);
    }
}