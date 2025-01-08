package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class WeatherServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        WeatherDataManager.loadWeatherData("ServerWeather/src/main/resources/weather_data.json");
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Serverul a pornit pe portul " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client conectat: " + clientSocket.getInetAddress());
            handleClient(clientSocket);
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        BufferedReader clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);

        String requestedLocation;

        while ((requestedLocation = clientInput.readLine()) != null) {
            System.out.println("Clientul a cerut: " + requestedLocation);
            if ("exit".equalsIgnoreCase(requestedLocation)) {
                break;
            }
            if ("admin".equalsIgnoreCase(requestedLocation)) {
                AdminHandler.handleAdmin(clientInput, clientOutput);
            } else {
                LocationHandler.handleLocationRequest(requestedLocation, clientInput, clientOutput);
            }
        }

        clientSocket.close();
        System.out.println("Client deconectat");
    }
}
