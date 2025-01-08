package org.example;

import org.json.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class WeatherDataManager {
    private static List<Location> locations = new ArrayList<>();
    private static final String WEATHER_DATA_PATH = "ServerWeather/src/main/resources/weather_data.json";

    public static void loadWeatherData(String filePath) throws IOException {
        Path path = Paths.get(filePath).toAbsolutePath();
        String content = new String(Files.readAllBytes(path));
        JSONObject json = new JSONObject(content);

        JSONArray locationsArray = json.getJSONArray("locations");
        for (int i = 0; i < locationsArray.length(); i++) {
            JSONObject locObj = locationsArray.getJSONObject(i);
            String name = locObj.getString("name");
            double latitude = locObj.getDouble("latitude");
            double longitude = locObj.getDouble("longitude");

            List<Forecast> forecastList = new ArrayList<>();
            JSONArray weatherArray = locObj.getJSONArray("weather");
            for (int j = 0; j < weatherArray.length(); j++) {
                JSONObject forecastObj = weatherArray.getJSONObject(j);
                forecastList.add(new Forecast(
                        forecastObj.getString("date"),
                        forecastObj.getString("condition"),
                        forecastObj.getDouble("temperature")
                ));
            }

            locations.add(new Location(name, latitude, longitude, forecastList));
        }
    }

    public static void saveWeatherData() {
        JSONObject json = new JSONObject();
        JSONArray locationsArray = new JSONArray();

        for (Location location : locations) {
            JSONObject locationObj = new JSONObject();
            locationObj.put("name", location.getName());
            locationObj.put("latitude", location.getLatitude());
            locationObj.put("longitude", location.getLongitude());

            JSONArray weatherArray = new JSONArray();
            for (Forecast forecast : location.getForecast()) {
                JSONObject forecastObj = new JSONObject();
                forecastObj.put("date", forecast.getDate());
                forecastObj.put("condition", forecast.getWeather());
                forecastObj.put("temperature", forecast.getTemperature());
                weatherArray.put(forecastObj);
            }
            locationObj.put("weather", weatherArray);
            locationsArray.put(locationObj);
        }
        json.put("locations", locationsArray);

        try (FileWriter file = new FileWriter(Paths.get(WEATHER_DATA_PATH).toAbsolutePath().toString())) {
            file.write(json.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String formatLocationName(String locationName) {
        if (locationName == null || locationName.isEmpty()) {
            return locationName;
        }
        return locationName.substring(0, 1).toUpperCase() + locationName.substring(1).toLowerCase();
    }

    public static Optional<Location> findLocation(String locationName) {
        return locations.stream().filter(loc -> loc.getName().equalsIgnoreCase(locationName)).findFirst();
    }

    public static void addLocation(Location location) {
        locations.add(location);
        saveWeatherData();
    }
}
