package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.Optional;

public class AdminHandler {
    private static final String ADMIN_PASSWORD = "admin1234";

    public static void handleAdmin(BufferedReader clientInput, PrintWriter clientOutput) throws IOException {
        String password = clientInput.readLine();
        if (ADMIN_PASSWORD.equals(password)) {
            clientOutput.println("GRANTED");
            handleAdminCommands(clientInput, clientOutput);
        } else {
            clientOutput.println("DENIED");
        }
    }

    private static void handleAdminCommands(BufferedReader clientInput, PrintWriter clientOutput) throws IOException {
        String command;
        while ((command = clientInput.readLine()) != null) {
            if ("exit admin".equalsIgnoreCase(command)) {
                break;
            } else if ("add location".equalsIgnoreCase(command)) {
                LocationHandler.addLocation(clientInput, clientOutput);
            } else if ("add forecast".equalsIgnoreCase(command)) {
                LocationHandler.addForecast(clientInput, clientOutput);
            } else {
                clientOutput.println("Comanda necunoscuta.");
            }
        }
    }
}
