package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.*;

public class WeatherDataManager {
    private static final String JSON_FILE_PATH = "ServerWeather/src/main/resources/weather_data.json";
    private static List<Location> locations = new ArrayList<>();


    public static void loadJsonData() {
        File file = new File(JSON_FILE_PATH);

        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("locations", new JsonArray());
                writer.write(jsonObject.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

                JsonArray locationsArray = jsonObject.getAsJsonArray("locations");
                locations.clear();

                for (int i = 0; i < locationsArray.size(); i++) {
                    JsonObject locationJson = locationsArray.get(i).getAsJsonObject();
                    String name = locationJson.get("name").getAsString();
                    double latitude = locationJson.get("latitude").getAsDouble();
                    double longitude = locationJson.get("longitude").getAsDouble();

                    Location location = new Location(name, latitude, longitude, new ArrayList<>());
                    JsonArray forecastArray = locationJson.has("forecast") ? locationJson.getAsJsonArray("forecast") : new JsonArray();

                    for (int j = 0; j < forecastArray.size(); j++) {
                        JsonObject forecastJson = forecastArray.get(j).getAsJsonObject();
                        String date = forecastJson.get("date").getAsString();
                        String weather = forecastJson.get("weather").getAsString();
                        double temperature = forecastJson.get("temperature").getAsDouble();
                        Forecast forecast = new Forecast(date, weather, temperature);
                        location.addForecast(forecast);
                    }
                    locations.add(location);

                    DatabaseManager.addLocationToDatabase(location);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveLocationsDataToJson() {
        JsonObject jsonObject = new JsonObject();
        JsonArray locationsArray = new JsonArray();

        for (Location location : locations) {
            JsonObject locationJson = new JsonObject();
            locationJson.addProperty("name", location.getName());
            locationJson.addProperty("latitude", location.getLatitude());
            locationJson.addProperty("longitude", location.getLongitude());

            JsonArray forecastArray = new JsonArray();
            for (Forecast forecast : location.getForecast()) {
                JsonObject forecastJson = new JsonObject();
                forecastJson.addProperty("date", forecast.getDate());
                forecastJson.addProperty("weather", forecast.getWeather());
                forecastJson.addProperty("temperature", forecast.getTemperature());
                forecastArray.add(forecastJson);
            }
            locationJson.add("forecast", forecastArray);
            locationsArray.add(locationJson);
        }
        jsonObject.add("locations", locationsArray);

        try (FileWriter writer = new FileWriter(JSON_FILE_PATH)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addLocation(Location location) {
        locations.add(location);
        saveLocationsDataToJson();
        DatabaseManager.addLocationToDatabase(location);
    }

    public static void addForecast(Location location, Forecast forecast) {
        location.addForecast(forecast);
        saveLocationsDataToJson();
        Integer locationId = DatabaseManager.getLocationIdFromDatabase(location);
        if (locationId != null) {
            DatabaseManager.addForecastToDatabase(forecast, locationId);
        }
    }
}
