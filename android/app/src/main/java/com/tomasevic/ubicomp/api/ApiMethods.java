package com.tomasevic.ubicomp.api;

import com.tomasevic.ubicomp.models.ApiResponseModel;
import com.tomasevic.ubicomp.models.DataArrayModel;
import com.tomasevic.ubicomp.models.DataModel;
import com.tomasevic.ubicomp.models.FilterModel;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by simon on 24.10.16..
 */

public interface ApiMethods {

    @GET("sensorData")
    Observable<DataArrayModel<DataModel>> getSensorData();

    @POST("sensorData")
    Observable<ApiResponseModel> sendSensorData(@Body DataArrayModel<DataModel> data);

    @POST("filterSensorData")
    Observable<DataArrayModel<DataModel>> filterSensorData(@Body DataArrayModel<FilterModel> filters);

    @GET("lastSensorData")
    Observable<DataArrayModel<DataModel>> getLastSensorData();

    @GET("maxSensorDataByDay")
    Observable<DataArrayModel<DataModel>> getMaxSensorDataByDay(@Query("timestamp") long timestamp);
}
