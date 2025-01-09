package org.example;

import java.io.*;
import java.net.Socket;

public class WeatherClient {
    private static final int SERVER_PORT = 8080;
    private static boolean isAdmin = false;

    public static void main(String[] args) {
        try (Socket clientSocket = new Socket("localhost", SERVER_PORT)) {
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter serverOutput = new PrintWriter(clientSocket.getOutputStream(), true);

            while (true) {
                String command = promptUser("Introduceti locatia dorita (sau 'exit' pentru a inchide, 'admin' pentru admin): ");
                handleCommand(command, serverInput, serverOutput);
            }
        } catch (IOException e) {
            System.err.println("Eroare de conectare la server: " + e.getMessage());
        }
    }

    private static void mainMenu() {
        System.out.println("=============================================");
        System.out.println(" - Program vreme - ");
        System.out.println("1. Introduceti o locatie pentru prognoza meteo");
        System.out.println("2. \"admin\" - Acces Admin");
        System.out.println("3. \"exit\" - Iesire din aplicatie");
        System.out.println("=============================================");
    }

    private static void handleCommand(String command, BufferedReader serverInput, PrintWriter serverOutput) throws IOException {
        switch (command.toLowerCase()) {
            case "exit":
                serverOutput.println("exit");
                break;
            case "admin":
                handleAdminLogin(serverInput, serverOutput);
                break;
            default:
                handleLocationRequest(command, serverInput, serverOutput);
                break;
        }
    }

    private static void handleAdminLogin(BufferedReader serverInput, PrintWriter serverOutput) throws IOException {
        String password = promptUser("Introduceti parola pentru admin: ");
        serverOutput.println("admin");
        serverOutput.println(password);

        String adminResponse = serverInput.readLine();
        System.out.println("ACCESS " + adminResponse);
        if ("GRANTED".equalsIgnoreCase(adminResponse)) {
            isAdmin = true;
            handleAdminCommands(serverInput, serverOutput);
            isAdmin = false;
        } else {
            System.out.println("Parola nu este corecta. Va rugam să incercati din nou.");
        }
    }

    private static void handleAdminCommands(BufferedReader serverInput, PrintWriter serverOutput) throws IOException {
        String command;
        while (true) {
            command = promptUser("Introduceti comanda tip admin (adauga locatie/actualizare/adauga prognoza sau 'exit admin' pentru a iesi): ");
            if ("exit admin".equalsIgnoreCase(command)) {
                serverOutput.println(command);
                break;
            }
            switch (command.toLowerCase()) {
                case "adauga locatie":
                    addLocation(serverInput, serverOutput);
                    break;
                case "actualizare":
                    updateLocation(serverInput, serverOutput);
                    break;
                case "adauga prognoza":
                    addForecast(serverInput, serverOutput);
                    break;
                default:
                    System.out.println("Comanda necunoscuta. Incercati din nou.");
                    break;
            }
        }
    }

    private static void handleLocationRequest(String location, BufferedReader serverInput, PrintWriter serverOutput) throws IOException {
        if (isAdmin) {
            System.out.println("Iesiti din modul administrator pentru a solicita o locatie.");
            return;
        }

        serverOutput.println(location);
        String response = serverInput.readLine();
        if ("Locatie necunoscuta".equalsIgnoreCase(response)) {
            System.out.println("Locatia introdusa nu este disponibila.");
        } else {
            printWeatherForecast(serverInput);
        }
    }

    private static void printWeatherForecast(BufferedReader serverInput) throws IOException {
        String line;
        while (!(line = serverInput.readLine()).equals("DONE")) {
            System.out.println(line);
        }
    }

    private static void addLocation(BufferedReader serverInput, PrintWriter serverOutput) throws IOException {
        String name = promptUser("Introduceti numele locatiei: ");
        String latitude = promptUser("Introduceti latitudinea: ");
        String longitude = promptUser("Introduceti longitudinea: ");

        serverOutput.println("adauga locatie");
        serverOutput.println(name);
        serverOutput.println(latitude);
        serverOutput.println(longitude);

        String response = serverInput.readLine();
        if (response.contains("deja exista")) {
            System.out.println(response);
        }
    }

    private static void updateLocation(BufferedReader serverInput, PrintWriter serverOutput) throws IOException {
        String name = promptUser("Introduceti numele locatiei de actualizat: ");
        String latitude = promptUser("Introcudecti noua latitudine: ");
        String longitude = promptUser("Introcudecti noua longitudine: ");

        serverOutput.println("actualizare");
        serverOutput.println(name);
        serverOutput.println(latitude);
        serverOutput.println(longitude);

        String response = serverInput.readLine();
        System.out.println(response);
    }

    private static void addForecast(BufferedReader serverInput, PrintWriter serverOutput) throws IOException {
        serverOutput.println("adauga prognoza");
        String location = promptUser("Introduceti numele locatiei pentru care doriti sa adaugati prognoza: ");
        serverOutput.println(location);

        String response = serverInput.readLine();
        if (!"OK".equalsIgnoreCase(response)) {
            System.out.println(response);
            return;
        } else {
            String date = promptUser("Introduceti data (YYYY-MM-DD): ");
            String condition = promptUser("Introduceti conditia meteo (ex: Soare, Ploaie, Vant): ");
            String temperature = promptUser("Introduceti temperatura estimata: ");

            serverOutput.println(date);
            serverOutput.println(condition);
            serverOutput.println(temperature);

            System.out.println(serverInput.readLine());
        }
    }

    private static String promptUser(String prompt) throws IOException {
        System.out.print(prompt);
        return new BufferedReader(new InputStreamReader(System.in)).readLine();
    }
}
