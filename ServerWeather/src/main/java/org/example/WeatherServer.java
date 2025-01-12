package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class WeatherServer {
    private static final int PORT = 8080;
    private static boolean isRunning = true;

    public static void main(String[] args) throws IOException, SQLException {
        WeatherDataManager.loadJsonData();

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Serverul a pornit pe portul " + PORT);

        while (isRunning) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client conectat: " + clientSocket.getInetAddress());
            handleClient(clientSocket);
        }

        serverSocket.close();
        System.out.println("Serverul a fost oprit.");
    }

    private static void handleClient(Socket clientSocket) throws IOException, SQLException {
        BufferedReader clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);
        String requestedLocation;

        while ((requestedLocation = clientInput.readLine()) != null) {
            System.out.println("Clientul a cerut: " + requestedLocation);
            if ("exit".equalsIgnoreCase(requestedLocation)) {
                isRunning = false;
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
