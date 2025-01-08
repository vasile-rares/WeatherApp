package org.example;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

    public String getCurrentDayWeatherAndNext3Days() {
        LocalDate today = LocalDate.now();
        List<Forecast> filteredForecast = forecast.stream()
                .filter(f -> LocalDate.parse(f.date).isEqual(today) || LocalDate.parse(f.date).isAfter(today))
                .limit(4)
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder("Locatie: ").append(name).append("\n");

        if (filteredForecast.isEmpty()) {
            sb.append("Nu exista prognoza pentru ziua curenta.\n");
        } else {
            boolean isFirstDay = true;
            for (Forecast f : filteredForecast) {
                if (LocalDate.parse(f.date).isEqual(today) && isFirstDay) {
                    sb.append("Prognoza pentru ziua de azi: ")
                            .append(f.weather).append(", ").append(f.temperature).append("°C\n");
                    isFirstDay = false;
                } else {
                    sb.append("Prognoza pentru ").append(f.date).append(": ")
                            .append(f.weather).append(", ").append(f.temperature).append("°C\n");
                }
            }
        }

        return sb.toString();
    }
}
