package com.tomasevic.ubicomp.database;

import android.content.Intent;
import android.widget.Toast;

import com.tomasevic.ubicomp.UbiCompApplication;
import com.tomasevic.ubicomp.api.ApiManager;
import com.tomasevic.ubicomp.models.ApiResponseModel;
import com.tomasevic.ubicomp.models.DataArrayModel;
import com.tomasevic.ubicomp.models.DataModel;

import java.io.Console;
import java.util.ArrayList;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Tomasevic on 9.11.2016..
 */
public class DBDataManager
{
    private ArrayList<DataModel> records;
    DBAdapter dbAdapter;

    private DBDataManager()
    {
        records = new ArrayList<>();

        dbAdapter = new DBAdapter(UbiCompApplication.getContext());
        dbAdapter.open();
        records = dbAdapter.getAllRecords();
        dbAdapter.close();
    }

    private static class SingletonHolder
    {
        public static final DBDataManager instance = new DBDataManager();
    }

    public static DBDataManager getInstance()
    {
        return SingletonHolder.instance;
    }

    public ArrayList<DataModel> getRecords()
    {
        if(records.isEmpty())
        {
            dbAdapter.open();
            records = dbAdapter.getAllRecords();
            dbAdapter.close();
        }
        return records;
    }

    public void addNewRecord(DataModel record)
    {
        records.add(record);
        dbAdapter.open();
        long id = dbAdapter.insertRecord(record);
        dbAdapter.close();
        record.setId(id);
    }

    public DataModel getRecord(int index)
    {
        return records.get(index);
    }

    public DataModel getLastResult()
    {
        if(!records.isEmpty())
            return records.get(records.size()-1);
        else
            return null;
    }

    public boolean clearDB()
    {
        dbAdapter.open();
        boolean ret = dbAdapter.clearDatabase();
        dbAdapter.close();
        records.clear();
        return ret;
    }

    public boolean syncData()
    {
        if(records.isEmpty()) return false;

        DataArrayModel<DataModel> data = new DataArrayModel<>(records);
        Observable<ApiResponseModel> sensorDataObservable = ApiManager.createInstance("").sendSensorData(data);
        sensorDataObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ApiResponseModel>()
                {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e)
                    {
                        if (e instanceof HttpException)
                        {
                            ApiResponseModel apiError = ApiManager.parseError(((HttpException) e).response());
                            Toast.makeText(UbiCompApplication.getContext(), apiError.getError(), Toast.LENGTH_SHORT).show();
                        } else
                        {
                            Toast.makeText(UbiCompApplication.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onNext(ApiResponseModel response)
                    {
                        Toast.makeText(UbiCompApplication.getContext(), response.getMessage() + " - " + records.size(), Toast.LENGTH_SHORT).show();
                        clearDB();
                    }
                });
        return true;
    }
}
