package com.dcgabriel.mygeocam;

public class Pic {

    private String title;
    private double longitude;
    private double latitude;
    private String date;

    public Pic() {

    }
    public Pic(String title, double latitude, double longitude, String date) {
        this.title = title;
        this.longitude = longitude;
        this.latitude = latitude;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }



}
