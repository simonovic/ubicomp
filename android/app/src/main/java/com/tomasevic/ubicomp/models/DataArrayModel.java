package com.tomasevic.ubicomp.models;

import java.util.ArrayList;

/**
 * Created by simon on 27.10.16..
 */

public class DataArrayModel<T> {

    private ArrayList<T> data;

    public DataArrayModel(ArrayList<T> data) {
        this.data = data;
    }

    public ArrayList<T> getData() {
        return data;
    }

    public void setData(ArrayList<T> data) {
        this.data = data;
    }
}
