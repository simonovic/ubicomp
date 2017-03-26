package com.tomasevic.ubicomp;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.tomasevic.ubicomp.api.ApiManager;
import com.tomasevic.ubicomp.services.DatabaseSyncService;

public class SettingsActivity extends AppCompatActivity
{
    EditText ip, syncInterval;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        findViews();

        final String initialIP = ApiManager.createInstance("").getBaseURL();
        final String initialInterval = Integer.toString(DatabaseSyncService.getInterval());

        ip.setText(initialIP);
        syncInterval.setText(initialInterval);

        submit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String ipAddress = ip.getText().toString();
                String syncTime = syncInterval.getText().toString();

                if(!ipAddress.isEmpty() && initialIP.compareTo(ipAddress) != 0)
                {
                    ApiManager apiManager = ApiManager.createInstance(ipAddress);

                    Intent broadcast = new Intent("Map is ready");
                    sendBroadcast(broadcast);

                    if(apiManager != null)
                        Snackbar.make(v, "IP address successfully changed", Snackbar.LENGTH_SHORT).show();
                }
                if(!syncTime.isEmpty() && initialInterval.compareTo(syncTime) != 0)
                {
                    DatabaseSyncService.setInterval(Integer.parseInt(syncInterval.getText().toString()));
                    Snackbar.make(v, "Sync interval successfully changed", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void findViews()
    {
        ip = (EditText)findViewById(R.id.ip_address);
        syncInterval = (EditText)findViewById(R.id.sync_timer);
        submit = (Button)findViewById(R.id.settings_button);
    }
}
