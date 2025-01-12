package org.example;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/weather_data";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1q2w3e";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void addLocationToDatabase(Location location) {
        if (!locationExistsInDatabase(location)) {
            String locationQuery = "INSERT INTO locations (name, latitude, longitude) VALUES (?, ?, ?) RETURNING id";
            try (Connection conn = getConnection()) {
                try (PreparedStatement stmt = conn.prepareStatement(locationQuery)) {
                    stmt.setString(1, location.getName());
                    stmt.setDouble(2, location.getLatitude());
                    stmt.setDouble(3, location.getLongitude());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        int locationId = rs.getInt("id");

                        // Adauga prognozele locației în baza de date
                        if (location.getForecast() != null && !location.getForecast().isEmpty()) {
                            for (Forecast forecast : location.getForecast()) {
                                addForecastToDatabase(forecast, locationId);
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void addForecastToDatabase(Forecast forecast, int locationId) {
        try (Connection conn = getConnection()) {
            String forecastQuery = "INSERT INTO forecasts (location_id, date, weather, temperature) VALUES (?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(forecastQuery)) {
                stmt.setInt(1, locationId);
                String forecastDate = forecast.getDate();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date utilDate = dateFormat.parse(forecastDate);
                Date sqlDate = new Date(utilDate.getTime());
                stmt.setDate(2, sqlDate);
                stmt.setString(3, forecast.getWeather());
                stmt.setDouble(4, forecast.getTemperature());
                stmt.executeUpdate();
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static Optional<Location> findLocation(String name) throws SQLException {
        String query = "SELECT l.name, l.latitude, l.longitude, f.date, f.weather, f.temperature " +
                "FROM locations l " +
                "LEFT JOIN forecasts f ON l.id = f.location_id " +
                "WHERE l.name = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, name);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String locationName = rs.getString("name");
                        double latitude = rs.getDouble("latitude");
                        double longitude = rs.getDouble("longitude");

                        // Adaugam prognozele în lista
                        List<Forecast> forecasts = new ArrayList<>();
                        do {
                            String date = rs.getString("date");
                            String weather = rs.getString("weather");
                            double temperature = rs.getDouble("temperature");

                            if (date != null && weather != null) {
                                Forecast forecast = new Forecast(date, weather, temperature);
                                forecasts.add(forecast);
                            }
                        } while (rs.next());

                        Location location = new Location(locationName, latitude, longitude, forecasts);
                        return Optional.of(location);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }
    }

    public static String getUpcomingWeather(Location location) {
        String query = "SELECT date, weather, temperature FROM forecasts " +
                "WHERE location_id = (SELECT id FROM locations WHERE name = ?) " +
                "AND date >= ?";

        List<Forecast> forecasts = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int days = 5;

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, location.getName());
                stmt.setDate(2, Date.valueOf(today));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String date = rs.getString("date");
                        String weather = rs.getString("weather");
                        double temperature = rs.getDouble("temperature");
                        forecasts.add(new Forecast(date, weather, temperature));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Eroare la accesarea bazei de date: " + e.getMessage());
            }

            Forecast todayForecast = forecasts.stream()
                    .filter(f -> LocalDate.parse(f.getDate()).isEqual(today))
                    .findFirst()
                    .orElse(null);

            List<Forecast> upcomingForecasts = forecasts.stream()
                    .filter(f -> LocalDate.parse(f.getDate()).isAfter(today))
                    .sorted((f1, f2) -> LocalDate.parse(f1.getDate()).compareTo(LocalDate.parse(f2.getDate())))
                    .limit(days)
                    .collect(Collectors.toList());


            StringBuilder sb = new StringBuilder("Locatie: ").append(location.getName()).append("\n");
            if (todayForecast == null && upcomingForecasts.isEmpty()) {
                sb.append("❌ Nu exista prognoza disponibila pentru urmatoarele zile. ❌\n");
            } else {
                if (todayForecast != null) {
                    sb.append("⛅ Prognoza pentru astazi:\n");
                    sb.append("👉 Astazi: ").append(todayForecast.getWeather())
                            .append(" | ").append(todayForecast.getTemperature()).append("°C\n");
                } else {
                    sb.append("❌ Nu exista prognoza pentru astazi. ❌\n");
                }
                if (!upcomingForecasts.isEmpty()) {
                    int daysToShow = Math.min(upcomingForecasts.size(), 5);
                    sb.append("\n📅 Prognoza pentru urmatoarele ").append(daysToShow).append(" zile:\n");
                    upcomingForecasts.forEach(f ->
                            sb.append("👉 ").append(f.getDate()).append(": ").append(f.getWeather())
                                    .append(" | ").append(f.getTemperature()).append("°C\n")
                    );
                } else {
                    sb.append("❌ Nu exista prognoza pentru urmatoarele ").append(days).append(" zile. ❌\n");
                }
            }
            return sb.toString();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Integer getLocationIdFromDatabase(Location location) {
        Integer locationId = null;
        try (Connection conn = getConnection()) {
            String query = "SELECT id FROM locations WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, location.getName());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    locationId = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return locationId;
    }

    public static boolean locationExistsInDatabase(Location location) {
        String query = "SELECT 1 FROM locations WHERE name = ? LIMIT 1";
        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, location.getName());

                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
