package org.example;

import java.util.List;

class Location {
    private String name;
    private double latitude;
    private double longitude;
    private List<Forecast> forecast;

    public Location(String name, double latitude, double longitude, List<Forecast> forecast) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.forecast = forecast;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<Forecast> getForecast() {
        return forecast;
    }

    public void addForecast(Forecast forecast) {
        this.forecast.add(forecast);
    }
}
