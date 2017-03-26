package com.tomasevic.ubicomp.models;

/**
 * Created by simon on 13.11.16..
 */

public class FilterModel {

    private String type;
    private Integer from;
    private Integer to;
    private Float distance;
    private Double lat;
    private Double lng;

    public FilterModel() { }

    public FilterModel(String type, Integer from, Integer to) {
        this.type = type;
        this.from = from;
        this.to = to;
    }

    public FilterModel(String type, Float distance, Double lat, Double lng) {
        this.type = type;
        this.distance = distance;
        this.lat = lat;
        this.lng = lng;
    }



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
