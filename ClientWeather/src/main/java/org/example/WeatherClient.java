package org.example;

import java.io.*;
import java.net.Socket;

public class WeatherClient {
    private static final int SERVER_PORT = 8080;
    public static void main(String[] args) {
        try (Socket clientSocket = new Socket("localhost", SERVER_PORT)) {
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter serverOutput = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            boolean isAdmin = false;

            while (true) {
                System.out.print("Introduceti locatia dorita (sau 'exit' pentru a inchide, 'admin' pentru admin): ");
                String command = userInput.readLine();

                if ("exit".equalsIgnoreCase(command)) {
                    serverOutput.println(command);
                    break;
                }

                if ("admin".equalsIgnoreCase(command)) {
                    System.out.print("Introduceti parola pentru modul admin: ");
                    String password = userInput.readLine();
                    serverOutput.println(command);
                    serverOutput.println(password);

                    String adminResponse = serverInput.readLine();
                    System.out.println("Raspunsul serverului: " + adminResponse);
                    if ("ACCEPTED".equalsIgnoreCase(adminResponse)) {
                        isAdmin = true;
                        handleAdminCommands(serverInput, serverOutput, userInput);
                        isAdmin = false;
                    } else {
                        System.out.println("Parola incorecta. Incercare esuata.");
                    }
                } else {
                    if (isAdmin) {
                        System.out.println("Trebuie sa iesi din modul admin inainte de a cere o locatie.");
                        continue;
                    }

                    String userLocation = command;
                    serverOutput.println(userLocation);
                    
//                    System.out.print("Doriti sa specificati o raza de cautare? (da/nu): ");
//                    String specifyRadius = userInput.readLine();
//                    if ("da".equalsIgnoreCase(specifyRadius)) {
//                        System.out.print("Introduceti raza de cautare (in kilometri): ");
//                        String radius = userInput.readLine();
//                        serverOutput.println(radius);
//                    } else {
//                        serverOutput.println("0"); // Raza implicita (0 km)
//                    }
                    
                    System.out.println("Prognoza meteo:");

                    while (true) {
                        String line = serverInput.readLine();
                        if (line == null || line.equals("DONE")) {
                            break;
                        }
                        System.out.println(line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Eroare de conectare la server: " + e.getMessage());
        }
    }

    private static void handleAdminCommands(BufferedReader in, PrintWriter out, BufferedReader userInput) throws IOException {
        String command;
        while (true) {
            System.out.print("Introduceti comanda tip admin (adauga locatie/actualizare/adauga prognoza sau 'exit admin' pentru a iesi): ");
            command = userInput.readLine();

            if ("exit admin".equalsIgnoreCase(command)) {
                out.println(command);
                break;
            }

            switch (command.toLowerCase()) {
                case "adauga locatie":
                    System.out.print("Introduceti numele locatiei: ");
                    String name = userInput.readLine();
                    System.out.print("Introduceti latitudinea: ");
                    String latitude = userInput.readLine();
                    System.out.print("Introduceti longitudinea: ");
                    String longitude = userInput.readLine();

                    out.println(command);
                    out.println(name);
                    out.println(latitude);
                    out.println(longitude);

                    String addResponse = in.readLine();
                    if (addResponse.contains("deja exista")) {
                        System.out.println(addResponse);
                    }
                    break;

                case "actualizare":
                    System.out.print("Introduceti numele locatiei de actualizat: ");
                    String updateName = userInput.readLine();
                    System.out.print("Latitudine: ");
                    String newLatitude = userInput.readLine();
                    System.out.print("Longitudine: ");
                    String newLongitude = userInput.readLine();

                    out.println(command);
                    out.println(updateName);
                    out.println(newLatitude);
                    out.println(newLongitude);

                    String updateResponse = in.readLine();
                    System.out.println(updateResponse);
                    break;

                case "adauga prognoza":
                    out.println(command);
                    System.out.print("Introduceti numele locatiei pentru care doriti sa adaugati prognoza: ");
                    String forecastLocation = userInput.readLine();
                    out.println(forecastLocation);

                    String locationResponse = in.readLine();
                    if (!"OK".equalsIgnoreCase(locationResponse)) {
                        System.out.println(locationResponse);
                        break;
                    } else {
                        System.out.print("Introduceti data (YYYY-MM-DD): ");
                        String date = userInput.readLine();
                        System.out.print("Introduceti conditia meteo (e.g., Soare, Ploaie): ");
                        String condition = userInput.readLine();
                        System.out.print("Introduceti temperatura: ");
                        String temperature = userInput.readLine();

                        out.println(date);
                        out.println(condition);
                        out.println(temperature);

                        String forecastResponse = in.readLine();
                        System.out.println(forecastResponse);
                    }
                    break;

                default:
                    System.out.println("Comanda necunoscuta. Incercati din nou.");
                    break;
            }
        }
    }
}
