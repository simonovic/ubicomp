package com.tomasevic.ubicomp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tomasevic.ubicomp.adapters.PagerAdapter;
import com.tomasevic.ubicomp.api.ApiManager;
import com.tomasevic.ubicomp.bluetooth_component.BluetoothActivity;
import com.tomasevic.ubicomp.fragments.ChartsFragment;
import com.tomasevic.ubicomp.fragments.DataMapFragment;
import com.tomasevic.ubicomp.fragments.DetailsFragment;
import com.tomasevic.ubicomp.models.ApiResponseModel;
import com.tomasevic.ubicomp.models.DataArrayModel;
import com.tomasevic.ubicomp.models.DataModel;
import com.tomasevic.ubicomp.models.FilterModel;
import com.tomasevic.ubicomp.services.DatabaseSyncService;

import java.util.ArrayList;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PERMISSION_CODE = 101;

    private double lat = 43.320412;
    private double lng = 21.900423;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private com.tomasevic.ubicomp.adapters.PagerAdapter pagerAdapter;

    private EditText editRadius;
    private Button btnFilter, btnRefresh;
    private TabLayout tabLayout;
    private ViewPager pager;

    private TextView txtFilterHumidity, txtFilterAirQuality; //TODO add filter for time
    private EditText editFromTemperature, editFromHumidity, editFromAirQuality,
            editToTemperature, editToHumidity, editToAirQuality;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            getDataFormServer();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        IntentFilter intentFilter = new IntentFilter("Map is ready");
        this.registerReceiver(broadcastReceiver, intentFilter);

        findViews();
        txtFilterHumidity.setText(getString(R.string.side_menu_humidity));
        txtFilterAirQuality.setText(getString(R.string.side_menu_air_quality));
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter();
                drawer.closeDrawer(Gravity.LEFT);
            }
        });
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDataFormServer();
                drawer.closeDrawer(Gravity.LEFT);
            }
        });

        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Details"));
        tabLayout.addTab(tabLayout.newTab().setText("Map"));
        tabLayout.addTab(tabLayout.newTab().setText("Charts"));

        pager = (ViewPager)findViewById(R.id.tabPager);
        pager.setOffscreenPageLimit(2);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new DataMapFragment(), "Map");
        pagerAdapter.addFragment(new ChartsFragment(), "Charts");
        pagerAdapter.addFragment(new DetailsFragment(), "Details");
        pager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(pager);

        setUpLocationListener();

        startService(new Intent(getApplicationContext(), DatabaseSyncService.class));
    }

    public void getDataFormServer()
    {
        Observable<DataArrayModel<DataModel>> dataObservable = ApiManager.createInstance("").getSensorData();
        dataObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<DataArrayModel<DataModel>>()
                {
                    @Override
                    public void onCompleted()
                    {
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        if (e instanceof HttpException) {
                            ApiResponseModel apiError = ApiManager.parseError(((HttpException) e).response());
                            showToast(apiError.getError());
                        } else {
                            showToast(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(DataArrayModel<DataModel> response)
                    {
                        if (response.getData().size() > 0) {
                            ((DataMapFragment) pagerAdapter.getItem(0)).setData(response.getData());
                            ((ChartsFragment) pagerAdapter.getItem(1)).setData(response.getData());
                        } else {
                            showToast("No data");
                        }
                    }
                });
    }

    private void setUpLocationListener() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lng = location.getLongitude();
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
    }

    private void setRequestLocationUpdates() {
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            lat = lastKnownLocation.getLatitude();
            lng = lastKnownLocation.getLongitude();
        }
        locationManager.requestLocationUpdates("gps", 3000, 0, locationListener);
    }

    private void findViews()  {
        editRadius = (EditText) findViewById(R.id.editRadius);
        View temperature = findViewById(R.id.includeTemperature);
        editFromTemperature = (EditText) temperature.findViewById(R.id.editFilterFrom);
        editToTemperature = (EditText) temperature.findViewById(R.id.editFilterTo);
        View humidity = findViewById(R.id.includeHumidity);
        txtFilterHumidity = (TextView) humidity.findViewById(R.id.txtFilterType);
        editFromHumidity = (EditText) humidity.findViewById(R.id.editFilterFrom);
        editToHumidity = (EditText) humidity.findViewById(R.id.editFilterTo);
        btnFilter = (Button) findViewById(R.id.btnFilter);
        btnRefresh = (Button) findViewById(R.id.btnRefresh);
        View airQuality = findViewById(R.id.includeAirQuality);
        txtFilterAirQuality = (TextView) airQuality.findViewById(R.id.txtFilterType);
        editFromAirQuality = (EditText) airQuality.findViewById(R.id.editFilterFrom);
        editToAirQuality = (EditText) airQuality.findViewById(R.id.editFilterTo);
    }

    private void filter() {
        ArrayList<FilterModel> filters = getFilters();
        if (filters.size() > 0) {
            Observable<DataArrayModel<DataModel>> filterObservable = ApiManager.createInstance("").filterSensorData(new DataArrayModel<>(filters));
            filterObservable.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<DataArrayModel<DataModel>>() {

                        @Override
                        public void onCompleted() { }

                        @Override
                        public void onError(Throwable e) {
                            if (e instanceof HttpException) {
                                ApiResponseModel apiError = ApiManager.parseError(((HttpException) e).response());
                                showToast(apiError.getError());
                            } else {
                                showToast(e.getMessage());
                            }
                        }

                        @Override
                        public void onNext(DataArrayModel<DataModel> response) {
                            if (response.getData().size() > 0) {
                                ((DataMapFragment)pagerAdapter.getItem(0)).setData(response.getData());
                                ((ChartsFragment)pagerAdapter.getItem(1)).setData(response.getData());
                            } else {
                                showToast("No data");
                            }
                        }
                    });
        }
    }

    private ArrayList<FilterModel> getFilters() {
        ArrayList<FilterModel> filters = new ArrayList<>();
        String radius = editRadius.getText().toString();
        if (!radius.equals("") && Integer.parseInt(radius) > 0) {
            FilterModel radiusFilter = new FilterModel("radius", Float.parseFloat(radius)/1000, lat, lng);
            filters.add(0, radiusFilter);
        }

        String from = editFromTemperature.getText().toString();
        String to = editToTemperature.getText().toString();
        if (!from.equals("") && !from.equals("0")) {
            if (to.equals("") || to.equals("0")) {
                showToast("Enter TO value for Temperature!");
            } else {
                FilterModel filter = new FilterModel("temperature", Integer.parseInt(from), Integer.parseInt(to));
                filters.add(filter);
            }
        }

        from = editFromHumidity.getText().toString();
        to = editToHumidity.getText().toString();
        if (!from.equals("") && !from.equals("0")) {
            if (to.equals("") || to.equals("0")) {
                showToast("Enter TO value for Humidity!");
            } else {
                FilterModel filter = new FilterModel("humidity", Integer.parseInt(from), Integer.parseInt(to));
                filters.add(filter);
            }
        }

        from = editFromAirQuality.getText().toString();
        to = editToAirQuality.getText().toString();
        if (!from.equals("") && !from.equals("0")) {
            if (to.equals("") || to.equals("0")) {
                showToast("Enter TO value for Air Quality!");
            } else {
                FilterModel filter = new FilterModel("air_quality", Integer.parseInt(from), Integer.parseInt(to));
                filters.add(filter);
            }
        }

        return filters;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        stopService(new Intent(MainActivity.this, DatabaseSyncService.class));
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            /*case R.id.action_data:
                startActivity(new Intent(this, DataActivity.class));
                break;*/
            case R.id.bluetooth:
                startActivity(new Intent(this, BluetoothActivity.class));
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /*if (id == R.id.nav_camera) {
            // Handle the camera action
        } */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
