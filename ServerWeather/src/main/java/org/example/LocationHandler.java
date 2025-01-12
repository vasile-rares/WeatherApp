package org.example;

import java.io.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Optional;

public class LocationHandler {

    public static void handleLocationRequest(String requestedLocation, BufferedReader clientInput, PrintWriter clientOutput) throws SQLException {
        Optional<Location> location = DatabaseManager.findLocation(requestedLocation);
        if (location.isPresent()) {
            Location loc = location.get();
            clientOutput.println(DatabaseManager.getUpcomingWeather(loc));
            clientOutput.println("DONE");
        } else {
            clientOutput.println("Locatie necunoscuta");
            clientOutput.println("DONE");
        }
    }

    public static void addLocation(BufferedReader clientInput, PrintWriter clientOutput) throws IOException, SQLException {
        String name = clientInput.readLine();
        Optional<Location> existingLocation = DatabaseManager.findLocation(name);
        if (existingLocation.isPresent()) {
            clientOutput.println("Eroare: Locatia deja exista si nu poate fi adaugata din nou.");
        } else {
            double latitude = Double.parseDouble(clientInput.readLine());
            double longitude = Double.parseDouble(clientInput.readLine());
            Location newLocation = new Location(name, latitude, longitude, new ArrayList<>());
            WeatherDataManager.addLocation(newLocation);
            clientOutput.println("Locatie adaugata cu succes.");
        }
    }

    public static void addForecast(BufferedReader clientInput, PrintWriter clientOutput) throws IOException, SQLException {
        String name = clientInput.readLine();
        Optional<Location> location = DatabaseManager.findLocation(name);
        if (location.isPresent()) {
            Location loc = location.get();
            if (loc.getForecast().size() >= 3) {
                clientOutput.println("Eroare: Aceasta locatie are deja 3 prognoze. Nu puteti adauga mai multe.");
                return;
            } else {
                clientOutput.println("OK");
            }

            String date = clientInput.readLine();
            if (!isValidDateFormat(date)) {
                clientOutput.println("Eroare: Formatul datei nu este corect. Trebuie sa fie in formatul YYYY-MM-DD.");
                return;
            }

            String weather = clientInput.readLine();
            double temperature = Double.parseDouble(clientInput.readLine());
            Forecast forecast = new Forecast(date, weather, temperature);
            WeatherDataManager.addForecast(loc, forecast);
            clientOutput.println("Prognoza adaugata cu succes.");
        } else {
            clientOutput.println("Locatia nu a fost gasita.");
        }
    }

    private static boolean isValidDateFormat(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);

        try {
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
