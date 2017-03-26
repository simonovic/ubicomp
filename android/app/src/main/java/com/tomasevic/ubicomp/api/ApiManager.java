package com.tomasevic.ubicomp.api;

import android.util.Log;

import com.tomasevic.ubicomp.models.ApiResponseModel;
import com.tomasevic.ubicomp.models.DataArrayModel;
import com.tomasevic.ubicomp.models.DataModel;
import com.tomasevic.ubicomp.models.FilterModel;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import okhttp3.ResponseBody;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import java.lang.annotation.Annotation;
import java.util.List;

import retrofit2.Response;
import rx.Observable;

/**
 * Created by simon on 24.10.16..
 */
public class ApiManager {
    public static final String protocol = "http://";
    public static final String port = ":5000/";
    public static String BASE_URL = "192.168.1.4";

    public static ApiManager apiManager = null;

    public static Retrofit retrofit = null;
    public static ApiMethods apiMethods;
    public static Converter<ResponseBody, ApiResponseModel> errorConverter;

    public static ApiManager createInstance(String url) {
        if (apiManager == null || !url.isEmpty()) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();


            if(!url.isEmpty())
                BASE_URL = url;
            retrofit = new Retrofit.Builder()
                    .baseUrl(protocol + BASE_URL + port)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();

            apiMethods = retrofit.create(ApiMethods.class);
            apiManager = new ApiManager();
            errorConverter = retrofit.responseBodyConverter(ApiResponseModel.class, new Annotation[0]);
        }
        return apiManager;
    }

    public static ApiResponseModel parseError(Response<?> response) {
        ApiResponseModel error;
        try {
            error = errorConverter.convert(response.errorBody());
        } catch (Exception e) {
            return new ApiResponseModel();
        }

        return error;
    }

    public Observable<DataArrayModel<DataModel>> getSensorData() {
        return apiMethods.getSensorData();
    }

    public Observable<ApiResponseModel> sendSensorData(DataArrayModel<DataModel> data) {
        return apiMethods.sendSensorData(data);
    }

    public Observable<DataArrayModel<DataModel>> getLastSensorData() {
        return apiMethods.getLastSensorData();
    }

    public Observable<DataArrayModel<DataModel>> getMaxSensorDataByDay(long timestamp)
    {
        return apiMethods.getMaxSensorDataByDay(timestamp);
    }

    public Observable<DataArrayModel<DataModel>> filterSensorData(DataArrayModel<FilterModel> filters) {
        return apiMethods.filterSensorData(filters);
    }

    public void setBaseURL(String ip)
    {
        BASE_URL = ip;
    }

    public String getBaseURL()
    {
        return BASE_URL;
    }
}
