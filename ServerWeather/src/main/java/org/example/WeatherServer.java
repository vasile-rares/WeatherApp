package org.example;

import org.json.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.net.ServerSocket;
import java.net.Socket;


public class WeatherServer {
    private static List<Location> locations = new ArrayList<>();
    private static final String WEATHER_DATA_PATH = "ServerWeather/src/main/resources/weather_data.json";
    private static final String ADMIN_PASSWORD = "admin1234";
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        loadWeatherData(WEATHER_DATA_PATH);
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Serverul a pornit pe portul " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client conectat: " + clientSocket.getInetAddress());
            handleClient(clientSocket);
        }
    }

    private static void loadWeatherData(String filePath) throws IOException {
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

    private static Optional<Location> findLocation(String locationName) {
        return locations.stream().filter(loc -> loc.getName().equalsIgnoreCase(locationName)).findFirst();
    }

    private static String formatLocationName(String locationName) {
        if (locationName == null || locationName.isEmpty()) {
            return locationName;
        }
        return locationName.substring(0, 1).toUpperCase() + locationName.substring(1).toLowerCase();
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String requestedLocation;

        while ((requestedLocation = in.readLine()) != null) {
            System.out.println("Clientul a cerut: " + requestedLocation);
            if ("exit".equalsIgnoreCase(requestedLocation)) {
                break;
            }
            if ("admin".equalsIgnoreCase(requestedLocation)) {
                String password = in.readLine();
                if (ADMIN_PASSWORD.equals(password)) {
                    out.println("ACCEPTED");
                    handleAdminCommands(in, out);
                } else {
                    out.println("REJECTED");
                }
            } else {
                Optional<Location> found = findLocation(requestedLocation);
                if (found.isPresent()) {
                    out.println(found.get().getUpcomingWeather());
                    out.println("DONE");
                } else {
                    out.println("Locatie necunoscuta");
                    out.println("DONE");
                }
            }
        }

        clientSocket.close();
        System.out.println("Client deconectat");
    }

    private static void handleAdminCommands(BufferedReader in, PrintWriter out) throws IOException {
        String command;
        while ((command = in.readLine()) != null) {
            if ("exit admin".equalsIgnoreCase(command)) {
                break;
            } else if ("adauga locatie".equalsIgnoreCase(command)) {
                String name = in.readLine();
                name = formatLocationName(name);
                Optional<Location> existingLocation = findLocation(name);
                if (existingLocation.isPresent()) {
                    out.println("Locatia deja exista si nu poate fi adaugata din nou.");
                } else {
                    double latitude = Double.parseDouble(in.readLine());
                    double longitude = Double.parseDouble(in.readLine());
                    locations.add(new Location(name, latitude, longitude, new ArrayList<>()));
                    saveWeatherData();
                    out.println("Locatie adaugata cu succes.");
                }
            } else if ("actualizare".equalsIgnoreCase(command)) {
                String name = in.readLine();
                Optional<Location> location = findLocation(name);
                if (location.isPresent()) {
                    Location loc = location.get();
                    double latitude = Double.parseDouble(in.readLine());
                    double longitude = Double.parseDouble(in.readLine());
                    loc.setLatitude(latitude);
                    loc.setLongitude(longitude);
                    saveWeatherData();
                    out.println("Locatie actualizata cu succes.");
                } else {
                    out.println("Locatia nu a fost gasita.");
                }
            } else if ("adauga prognoza".equalsIgnoreCase(command)) {
                String name = in.readLine();
                Optional<Location> location = findLocation(name);
                if (location.isPresent()) {
                    Location loc = location.get();
                    if (loc.getForecast().size() >= 3) {
                        out.println("Eroare: Această locatie are deja 3 prognoze. Nu puteti adauga mai multe.");
                        continue;
                    } else {
                        out.println("OK");
                    }
                    String date = in.readLine();
                    String condition = in.readLine();
                    double temperature = Double.parseDouble(in.readLine());

                    loc.getForecast().add(new Forecast(date, condition, temperature));
                    saveWeatherData();
                    out.println("Prognoza a fost adaugata cu succes.");
                } else {
                    out.println("Locatia nu a fost gasita.");
                }
            } else {
                out.println("Comanda necunoscuta.");
            }
        }
    }

    private static void saveWeatherData() {
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
                forecastObj.put("date", forecast.date);
                forecastObj.put("condition", forecast.weather);
                forecastObj.put("temperature", forecast.temperature);
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
}

