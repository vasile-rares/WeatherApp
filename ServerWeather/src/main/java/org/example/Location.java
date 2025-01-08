package org.example;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

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

    public String getUpcomingWeather() {
        LocalDate today = LocalDate.now();
        List<Forecast> filteredForecast = forecast.stream()
                .filter(f -> !LocalDate.parse(f.getDate()).isBefore(today))
                .sorted(Comparator.comparing(f -> LocalDate.parse(f.getDate())))
                .limit(3)
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder("Locatie: ").append(name).append("\n");

        if (filteredForecast.isEmpty()) {
            sb.append("Nu exista prognoza pentru ziua curenta.\n");
        } else {
            for (Forecast f : filteredForecast) {
                String datePrefix = (LocalDate.parse(f.getDate()).isEqual(today)) ? "Prognoza pentru ziua de azi: " : "Prognoza pentru " + f.getDate() + ": ";
                sb.append(datePrefix).append(f.getWeather()).append(", ").append(f.getTemperature()).append("°C\n");
            }
        }

        return sb.toString();
    }
}
