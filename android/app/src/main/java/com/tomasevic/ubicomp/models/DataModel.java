package com.tomasevic.ubicomp.models;

/**
 * Created by simon on 24.10.16..
 */
public class DataModel {

    private Long id;
    private Integer temperature;
    private Integer humidity;
    private Integer air_quality;
    private Float heat_index;
    private Double lat;
    private Double lng;
    private Long timestamp;

    public DataModel(long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTemperature() {
        return temperature;
    }

    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public Integer getAir_quality() {
        return air_quality;
    }

    public void setAir_quality(Integer air_quality) {
        this.air_quality = air_quality;
    }

    public Float getHeat_index() {
        return heat_index;
    }

    public void setHeat_index(Float heat_index) {
        this.heat_index = heat_index;
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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
