package org.example;

import org.json.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;

public class WeatherServer {
    private static List<Location> locations = new ArrayList<>();
    private static String WEATHER_DATA_FILE = "C:\\Users\\CNAE\\Downloads\\ServerWeather\\src\\main\\resources\\weather_data.json";
    private static String ADMIN_PASSWORD = "admin1234";

    public static void main(String[] args) throws IOException {
        loadWeatherData(WEATHER_DATA_FILE);
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Serverul e pornit pe portul 12345");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client conectat de la: " + clientSocket.getInetAddress());
            handleClient(clientSocket);
        }
    }

    private static void loadWeatherData(String fileName) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(fileName)));
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
                    out.println(found.get().getCurrentDayWeatherAndNext3Days());
                    out.println("END");
                } else {
                    out.println("Locatie necunoscuta");
                    out.println("END");
                }
            }
        }

        clientSocket.close();
        System.out.println("Client deconectat");
    }

    private static void handleAdminCommands(BufferedReader in, PrintWriter out) throws IOException {
        String command;
        while ((command = in.readLine()) != null) {
            if ("exitadmin".equalsIgnoreCase(command)) {
                break;
            } else if ("adaugare".equalsIgnoreCase(command)) {
                String name = in.readLine();
                double latitude = Double.parseDouble(in.readLine());
                double longitude = Double.parseDouble(in.readLine());

                locations.add(new Location(name, latitude, longitude, new ArrayList<>()));
                saveWeatherData();
                out.println("Locatie adaugata cu succes.");
            } else if ("actualizare".equalsIgnoreCase(command)) {
                String name = in.readLine();
                Optional<Location> locOpt = findLocation(name);
                if (locOpt.isPresent()) {
                    Location loc = locOpt.get();
                    double latitude = Double.parseDouble(in.readLine());
                    double longitude = Double.parseDouble(in.readLine());
                    loc.setLatitude(latitude);
                    loc.setLongitude(longitude);
                    saveWeatherData();
                    out.println("Locatie actualizata cu succes.");
                } else {
                    out.println("Locatia nu a fost gasita.");
                }
            } else if ("adaugaprognoza".equalsIgnoreCase(command)) {
                String name = in.readLine();
                Optional<Location> locOpt = findLocation(name);
                if (locOpt.isPresent()) {
                    Location loc = locOpt.get();
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

    private static Optional<Location> findLocation(String locationName) {
        return locations.stream()
                .filter(loc -> loc.getName().equalsIgnoreCase(locationName))
                .findFirst();
    }

    private static void saveWeatherData() {
        JSONObject json = new JSONObject();
        JSONArray locationsArray = new JSONArray();

        for (Location loc : locations) {
            JSONObject locObj = new JSONObject();
            locObj.put("name", loc.getName());
            locObj.put("latitude", loc.getLatitude());
            locObj.put("longitude", loc.getLongitude());

            JSONArray weatherArray = new JSONArray();
            for (Forecast f : loc.getForecast()) {
                JSONObject forecastObj = new JSONObject();
                forecastObj.put("date", f.date);
                forecastObj.put("condition", f.weather);
                forecastObj.put("temperature", f.temperature);
                weatherArray.put(forecastObj);
            }
            locObj.put("weather", weatherArray);
            locationsArray.put(locObj);
        }

        json.put("locations", locationsArray);

        try (FileWriter file = new FileWriter(WEATHER_DATA_FILE)) {
            file.write(json.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

