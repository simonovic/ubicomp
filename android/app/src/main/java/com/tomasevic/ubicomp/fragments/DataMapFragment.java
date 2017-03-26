package com.tomasevic.ubicomp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tomasevic.ubicomp.R;
import com.tomasevic.ubicomp.models.DataModel;

import java.util.ArrayList;

/**
 * Created by Tomasevic on 14.11.2016..
 */

public class DataMapFragment extends Fragment implements OnMapReadyCallback
{
    private static View view;

    private GoogleMap map;
    ArrayList<DataModel> data;
    LayoutInflater inflater;

    public void setData(ArrayList<DataModel> data)
    {
        this.data = data;
        setMarkers();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        this.inflater = inflater;

        view = inflater.inflate(R.layout.map_fragment_layout, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        map.setInfoWindowAdapter(new MyInfoWindowAdapter());

        Intent broadcast = new Intent("Map is ready");
        getActivity().sendBroadcast(broadcast);
    }

    private void setMarkers()
    {
        if(map != null && data != null)
        {
            map.clear();
            zoom(new LatLng(data.get(0).getLat(), data.get(0).getLng()), 13f);
            for(DataModel marker: data)
            {
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(marker.getLat(), marker.getLng()))
                        .snippet(getInfo(marker)));
            }
        }
    }

    private void zoom(LatLng point, float index)
    {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(point).zoom(index).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.moveCamera(cameraUpdate);
    }

    private String getInfo(DataModel dataModel)
    {
        String info = "temperature: " + dataModel.getTemperature() + "°C\n";
        info += "humidity: " + dataModel.getHumidity() + "%\n";
        info += "air quality: " + dataModel.getAir_quality();
        return  info;
    }


    public class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter
    {
        private final View contentsView;

        MyInfoWindowAdapter()
        {
            contentsView = inflater.inflate(R.layout.info_window, null);
            contentsView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    v.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public View getInfoWindow(Marker marker)
        {
            TextView tempTxt = ((TextView)contentsView.findViewById(R.id.description_txt));
            tempTxt.setText(marker.getSnippet());

            return contentsView;
        }

        @Override
        public View getInfoContents(Marker marker)
        {
            return null;
        }
    }
}
