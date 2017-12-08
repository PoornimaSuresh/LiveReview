package com.mobile.project.livereview.entity;

/**
 * Created by johannes on 12/3/17.
 */

public class MarkerLocation {
    private String uid;
    private double lat;
    private double lng;
    private String user;
    private String message;

    private String address;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public String getAddress() {
        return address;
    }
}
