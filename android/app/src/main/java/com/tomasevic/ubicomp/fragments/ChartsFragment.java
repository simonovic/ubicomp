package com.tomasevic.ubicomp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.tomasevic.ubicomp.R;
import com.tomasevic.ubicomp.models.DataModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Tomasevic on 14.11.2016..
 */

public class ChartsFragment extends Fragment
{
    private static View view;

    BarChart barChart;
    List<BarEntry> tempEntries;
    List<BarEntry> humidityEntries;
    List<BarEntry> airQualityEntries;

    ArrayList<DataModel> data;

    public void setData(ArrayList<DataModel> data)
    {
        this.data = data;
        setData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.charts_fragment_layout, container, false);

        barChart = (BarChart) view.findViewById(R.id.line_chart);
        tempEntries = new ArrayList<>();
        humidityEntries = new ArrayList<>();
        airQualityEntries = new ArrayList<>();

        return view;
    }

    public void setData()
    {
        int ind=0;
        final ArrayList<String> dates = new ArrayList<>();
        tempEntries.clear();
        humidityEntries.clear();
        airQualityEntries.clear();

        for(DataModel element: data)
        {
            tempEntries.add(new BarEntry(ind, element.getTemperature()));
            humidityEntries.add(new BarEntry(ind, element.getHumidity()));
            airQualityEntries.add(new BarEntry(ind++, element.getAir_quality()/10));
            dates.add(timestampToDate(element.getTimestamp()));
        }

        IAxisValueFormatter formatter = new IAxisValueFormatter()
        {
            @Override
            public String getFormattedValue(float value, AxisBase axis)
            {
                if(value < dates.size())
                    return dates.get((int)value);
                else
                    return "";
            }

            @Override
            public int getDecimalDigits()
            {
                return 0;
            }
        };

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setTextSize(10f);
        xAxis.setValueFormatter(formatter);

        BarDataSet tempDataSet = new BarDataSet(tempEntries, "Temperature");
        BarDataSet humidityDataSet = new BarDataSet(humidityEntries, "Humidity");
        BarDataSet airQualityDataSet = new BarDataSet(airQualityEntries, "Air quality");

        tempDataSet.setColor(ColorTemplate.COLORFUL_COLORS[0]);
        humidityDataSet.setColor(ColorTemplate.COLORFUL_COLORS[2]);
        airQualityDataSet.setColor(ColorTemplate.COLORFUL_COLORS[4]);


        float groupSpace = 0.07f;
        float barSpace = 0.01f;
        float barWidth = 0.30f;

        BarData data = new BarData(tempDataSet, humidityDataSet, airQualityDataSet);

        data.setBarWidth(barWidth);

        barChart.setData(data);
        barChart.setVisibleXRangeMaximum(5);
        Description barDescription = new Description();
        barDescription.setText("Time and location based chart");
        barChart.setDescription(barDescription);
        barChart.groupBars(-0.5f, groupSpace, barSpace);
        barChart.invalidate();
    }

    public String timestampToDate(long timestamp)
    {
        try
        {
            String myFormat = "dd/MM HH:mm";
            Date date = new Date(timestamp * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            String tmp =sdf.format(date);
            return tmp;
        }
        catch (Exception e)
        {
            Log.w("timestamp to date", "error in conversion");
        }
        return  "";
    }
}
