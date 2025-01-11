package org.example;

import com.google.gson.Gson;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WeatherDataManager {

    // Adaugă o locație în baza de date și returnează ID-ul acesteia
    public static int addLocation(String name, double latitude, double longitude) {
        String insertLocation = "INSERT INTO locations (name, latitude, longitude) VALUES (?, ?, ?) RETURNING id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(insertLocation)) {

            stmt.setString(1, name);
            stmt.setDouble(2, latitude);
            stmt.setDouble(3, longitude);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1); // Returnează ID-ul locației
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Eroare
    }

    // Adaugă prognoze pentru o locație existentă
    public static void addForecast(String name, Forecast forecast) {
        String findLocationId = "SELECT id FROM locations WHERE name = ?";
        String insertForecast = "INSERT INTO forecasts (location_id, date, weather, temperature) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement findStmt = connection.prepareStatement(findLocationId);
             PreparedStatement insertStmt = connection.prepareStatement(insertForecast)) {

            findStmt.setString(1, name);
            ResultSet resultSet = findStmt.executeQuery();

            if (resultSet.next()) {
                int locationId = resultSet.getInt("id");

                // Adaugă prognoza în baza de date
                insertStmt.setInt(1, locationId);
                insertStmt.setString(2, forecast.getDate());
                insertStmt.setString(3, forecast.getWeather());
                insertStmt.setDouble(4, forecast.getTemperature());
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Încarcă locația și prognozele asociate
    public static Optional<Location> findLocation(String name) {
        String selectLocation = "SELECT id, latitude, longitude FROM locations WHERE name = ?";
        String selectForecasts = "SELECT date, weather, temperature FROM forecasts WHERE location_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement locationStmt = connection.prepareStatement(selectLocation);
             PreparedStatement forecastStmt = connection.prepareStatement(selectForecasts)) {

            // Găsește locația
            locationStmt.setString(1, name);
            ResultSet locationResult = locationStmt.executeQuery();

            if (locationResult.next()) {
                int locationId = locationResult.getInt("id");
                double latitude = locationResult.getDouble("latitude");
                double longitude = locationResult.getDouble("longitude");

                // Găsește prognozele
                forecastStmt.setInt(1, locationId);
                ResultSet forecastResult = forecastStmt.executeQuery();

                List<Forecast> forecasts = new ArrayList<>();
                while (forecastResult.next()) {
                    forecasts.add(new Forecast(
                            forecastResult.getString("date"),
                            forecastResult.getString("weather"),
                            forecastResult.getDouble("temperature")
                    ));
                }

                Location location = new Location(locationId, name, latitude, longitude, forecasts);
                return Optional.of(location); // Returnează locația într-un Optional
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty(); // Dacă nu s-a găsit locația, returnează un Optional gol
    }

}
