package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;

public class LocationHandler {

    public static void handleLocationRequest(String requestedLocation, BufferedReader clientInput, PrintWriter clientOutput) throws IOException {
        Optional<Location> location = WeatherDataManager.findLocation(requestedLocation);
        if (location.isPresent()) {
            Location loc = location.get();
            clientOutput.println(loc.getUpcomingWeather());
            clientOutput.println("DONE");
        } else {
            clientOutput.println("Locatie necunoscuta");
            clientOutput.println("DONE");
        }
    }

    public static void addLocation(BufferedReader clientInput, PrintWriter clientOutput) throws IOException {
        String name = clientInput.readLine();
        Optional<Location> existingLocation = WeatherDataManager.findLocation(name);
        if (existingLocation.isPresent()) {
            clientOutput.println("Locatia deja exista si nu poate fi adaugata din nou.");
        } else {
            double latitude = Double.parseDouble(clientInput.readLine());
            double longitude = Double.parseDouble(clientInput.readLine());
            WeatherDataManager.addLocation(name, latitude, longitude);
            clientOutput.println("Locatie adaugata cu succes.");
        }
    }

    public static void addForecast(BufferedReader clientInput, PrintWriter clientOutput) throws IOException {
        String name = clientInput.readLine();
        Optional<Location> location = WeatherDataManager.findLocation(name);
        if (location.isPresent()) {
            Location loc = location.get();
            if (loc.getForecast().size() >= 3) {
                clientOutput.println("Eroare: Aceasta locatie are deja 3 prognoze. Nu puteti adauga mai multe.");
                return;
            } else {
                clientOutput.println("OK");
            }
            String date = clientInput.readLine();
            String weather = clientInput.readLine();
            double temperature = Double.parseDouble(clientInput.readLine());

            Forecast forecast = new Forecast(date, weather, temperature);
            WeatherDataManager.addForecast(loc.getName(), forecast);
            clientOutput.println("Prognoza adaugata cu succes.");
        } else {
            clientOutput.println("Locatia nu a fost gasita.");
        }
    }
}
