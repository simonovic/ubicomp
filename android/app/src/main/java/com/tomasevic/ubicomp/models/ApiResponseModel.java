package com.tomasevic.ubicomp.models;

/**
 * Created by simon on 24.10.16..
 */
public class ApiResponseModel {

    private String message;
    private String error;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
