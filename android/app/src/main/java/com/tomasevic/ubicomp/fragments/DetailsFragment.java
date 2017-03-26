package com.tomasevic.ubicomp.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.tomasevic.ubicomp.R;
import com.tomasevic.ubicomp.api.ApiManager;
import com.tomasevic.ubicomp.database.DBDataManager;
import com.tomasevic.ubicomp.models.ApiResponseModel;
import com.tomasevic.ubicomp.models.DataArrayModel;
import com.tomasevic.ubicomp.models.DataModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by Tomasevic on 19.12.2016..
 */

public class DetailsFragment extends Fragment
{
    private static View view;

    TextView lastTemp, lastHumidity, lastAirQuality, lastDate, selectDate;
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener date;
    BarChart barChart;

    DataModel lastResult;
    List<BarEntry> tempEntries;
    List<BarEntry> humidityEntries;
    List<BarEntry> airQualityEntries;
    ArrayList<DataModel> data;
    boolean isLocalResult = false;
    long timestamp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.details_fragment_layout, container, false);

        lastTemp = (TextView) view.findViewById(R.id.last_temp);
        lastHumidity = (TextView) view.findViewById(R.id.last_humidity);
        lastAirQuality = (TextView) view.findViewById(R.id.last_air_quality);
        lastDate = (TextView)view.findViewById(R.id.last_date);
        selectDate = (TextView)view.findViewById(R.id.select_date);

        barChart = (BarChart)view.findViewById(R.id.bar_chart_max_temp);
        tempEntries = new ArrayList<>();
        humidityEntries = new ArrayList<>();
        airQualityEntries = new ArrayList<>();

        DataModel lastResult = DBDataManager.getInstance().getLastResult();
        if (lastResult != null)
        {
            this.lastResult = lastResult;
            lastTemp.setText(String.valueOf(lastResult.getTemperature()) + "°C");
            lastHumidity.setText(String.valueOf(lastResult.getHumidity()) + "%");
            lastAirQuality.setText(String.valueOf(lastResult.getAir_quality()));
            lastDate.setText(formattedDate(lastResult.getTimestamp()));
            isLocalResult = true;
        } else
        {

        }

        calendar = Calendar.getInstance();
        date = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "dd/MM/yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                timestamp = calendar.getTime().getTime() / 1000;
                selectDate.setText("Selected date: " + sdf.format(calendar.getTime()));

                resetChartData();
                getMaxSensorDataByDay(timestamp);
            }
        };

        selectDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new DatePickerDialog(getActivity(), date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        return view;
    }

    public void resetChartData()
    {
        tempEntries.clear();
        humidityEntries.clear();
        airQualityEntries.clear();
    }

    public void getMaxSensorDataByDay(long timestamp)
    {
        Observable<DataArrayModel<DataModel>> dataObservable = ApiManager.createInstance("").getMaxSensorDataByDay(timestamp);
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
                        if (e instanceof HttpException)
                        {
                            ApiResponseModel apiError = ApiManager.parseError(((HttpException) e).response());
                            showToast(apiError.getError());
                        } else
                        {
                            showToast(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(DataArrayModel<DataModel> response)
                    {
                        if (response.getData().size() > 0)
                        {
                            DataModel model = response.getData().get(0);
                            setData(model);
                        } else
                        {
                            showToast("No data");
                        }
                    }
                });
    }

    @Override
    public void onResume()
    {
        if(!isLocalResult)
            getLastSensorDataFromServer();
        super.onResume();
    }

    public void getLastSensorDataFromServer()
    {
        Observable<DataArrayModel<DataModel>> dataObservable = ApiManager.createInstance("").getLastSensorData();
        dataObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<DataArrayModel<DataModel>>()
                {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e)
                    {
                        if (e instanceof HttpException)
                        {
                            ApiResponseModel apiError = ApiManager.parseError(((HttpException) e).response());
                            showToast(apiError.getError());
                        } else
                        {
                            showToast(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(DataArrayModel<DataModel> response)
                    {
                        if (response.getData().size() > 0)
                        {
                            DataModel model = response.getData().get(0);
                            lastResult = model;
                            lastTemp.setText(String.valueOf(model.getTemperature()) + "°C");
                            lastHumidity.setText(String.valueOf(model.getHumidity()) + "%");
                            lastAirQuality.setText(String.valueOf(model.getAir_quality()));
                            lastDate.setText(formattedDate(model.getTimestamp()));
                        } else
                        {
                            showToast("No data");
                        }
                    }
                });
    }

    private void showToast(String message)
    {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private String formattedDate(long timestamp)
    {
        String myFormat = "dd/MM/yyyy HH:mm";

        Date date = new Date(timestamp * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        String tmp =sdf.format(date);
        return tmp;
    }

    private String formattedDateWithoutTime(long timestamp)
    {
        String myFormat = "dd/MM/yyyy";

        Date date = new Date(timestamp * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        String tmp =sdf.format(date);
        return tmp;
    }

    private void setData(DataModel dataModel)
    {
        final ArrayList<String> dates = new ArrayList<>();
        tempEntries.clear();

        tempEntries.add(new BarEntry(0, lastResult.getTemperature()));
        humidityEntries.add(new BarEntry(0, lastResult.getHumidity()));
        airQualityEntries.add(new BarEntry(0, lastResult.getAir_quality()/10));
        dates.add(formattedDate(lastResult.getTimestamp()));

        tempEntries.add(new BarEntry(1, dataModel.getTemperature()));
        humidityEntries.add(new BarEntry(1, dataModel.getHumidity()));
        airQualityEntries.add(new BarEntry(1, dataModel.getAir_quality()/10));
        dates.add(formattedDateWithoutTime(this.timestamp));

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
}
