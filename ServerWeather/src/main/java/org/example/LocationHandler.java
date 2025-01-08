package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;

public class LocationHandler {

    public static void handleLocationRequest(String requestedLocation, BufferedReader clientInput, PrintWriter clientOutput) throws IOException {
        Optional<Location> found = WeatherDataManager.findLocation(requestedLocation);
        if (found.isPresent()) {
            clientOutput.println(found.get().getUpcomingWeather());
            clientOutput.println("DONE");
        } else {
            clientOutput.println("Locatie necunoscuta");
            clientOutput.println("DONE");
        }
    }

    public static void addLocation(BufferedReader clientInput, PrintWriter clientOutput) throws IOException {
        String name = clientInput.readLine();
        name = WeatherDataManager.formatLocationName(name);
        Optional<Location> existingLocation = WeatherDataManager.findLocation(name);
        if (existingLocation.isPresent()) {
            clientOutput.println("Locatia deja exista si nu poate fi adaugata din nou.");
        } else {
            double latitude = Double.parseDouble(clientInput.readLine());
            double longitude = Double.parseDouble(clientInput.readLine());
            WeatherDataManager.addLocation(new Location(name, latitude, longitude, new ArrayList<>()));
            clientOutput.println("Locatie adaugata cu succes.");
        }
    }

    public static void updateLocation(BufferedReader clientInput, PrintWriter clientOutput) throws IOException {
        String name = clientInput.readLine();
        Optional<Location> location = WeatherDataManager.findLocation(name);
        if (location.isPresent()) {
            Location loc = location.get();
            double latitude = Double.parseDouble(clientInput.readLine());
            double longitude = Double.parseDouble(clientInput.readLine());
            loc.setLatitude(latitude);
            loc.setLongitude(longitude);
            WeatherDataManager.saveWeatherData();
            clientOutput.println("Locatie actualizata cu succes.");
        } else {
            clientOutput.println("Locatia nu a fost gasita.");
        }
    }

    public static void addForecast(BufferedReader clientInput, PrintWriter clientOutput) throws IOException {
        String name = clientInput.readLine();
        Optional<Location> location = WeatherDataManager.findLocation(name);
        if (location.isPresent()) {
            Location loc = location.get();
            if (loc.getForecast().size() >= 3) {
                clientOutput.println("Eroare: Această locatie are deja 3 prognoze. Nu puteti adauga mai multe.");
                return;
            } else {
                clientOutput.println("OK");
            }
            String date = clientInput.readLine();
            String condition = clientInput.readLine();
            double temperature = Double.parseDouble(clientInput.readLine());

            loc.getForecast().add(new Forecast(date, condition, temperature));
            WeatherDataManager.saveWeatherData();
            clientOutput.println("Prognoza adaugata cu succes.");
        } else {
            clientOutput.println("Locatia nu a fost gasita.");
        }
    }
}
