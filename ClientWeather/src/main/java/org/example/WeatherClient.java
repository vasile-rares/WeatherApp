package org.example;

import java.io.*;
import java.net.Socket;

public class WeatherClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String location;
            boolean isAdminMode = false;

            while (true) {
                System.out.print("Introduceți locația dorită (sau 'exit' pentru a închide, 'admin' pentru admin): ");
                location = userInput.readLine();

                if ("exit".equalsIgnoreCase(location)) {
                    out.println(location);
                    break;
                }

                if ("admin".equalsIgnoreCase(location)) {
                    System.out.print("Introduceți parola pentru modul admin: ");
                    String password = userInput.readLine();
                    out.println(location);
                    out.println(password);

                    String adminResponse = in.readLine();
                    if ("ACCEPTED".equalsIgnoreCase(adminResponse)) {
                        isAdminMode = true;
                        handleAdminCommands(in, out, userInput);
                        isAdminMode = false;
                    } else {
                        System.out.println("Parolă incorectă. Încercare eșuată.");
                    }
                } else {
                    if (isAdminMode) {
                        System.out.println("Trebuie să ieși din modul admin înainte de a cere o locație.");
                        continue;
                    }

                    // Trimite locația către server
                    out.println(location);

                    // Opțiune pentru raza de căutare
                    System.out.print("Doriți să specificați o rază de căutare? (da/nu): ");
                    String specifyRadius = userInput.readLine();
                    if ("da".equalsIgnoreCase(specifyRadius)) {
                        System.out.print("Introduceți raza de căutare (în kilometri): ");
                        String radius = userInput.readLine();
                        out.println(radius);
                    } else {
                        out.println("0"); // Rază implicită (0 km)
                    }

                    // Primește prognoza meteo de la server
                    System.out.println("Prognoza meteo:");
                    String line;
                    while (true) {
                        line = in.readLine();
                        if (line == null || line.equals("END")) {
                            break;
                        }
                        System.out.println(line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Eroare la conectarea la server: " + e.getMessage());
        }
    }

    private static void handleAdminCommands(BufferedReader in, PrintWriter out, BufferedReader userInput) throws IOException {
        String command;
        while (true) {
            System.out.print("Introduceți comanda admin (adaugare/actualizare/adaugaprognoza sau 'exitadmin' pentru a ieși): ");
            command = userInput.readLine();

            if ("exitadmin".equalsIgnoreCase(command)) {
                out.println(command);
                break;
            }

            out.println(command);
            switch (command.toLowerCase()) {
                case "adaugare":
                    System.out.print("Introduceți numele locației: ");
                    String name = userInput.readLine();
                    System.out.print("Introduceți latitudinea: ");
                    String latitude = userInput.readLine();
                    System.out.print("Introduceți longitudinea: ");
                    String longitude = userInput.readLine();

                    out.println(name);
                    out.println(latitude);
                    out.println(longitude);

                    String addResponse = in.readLine();
                    System.out.println(addResponse);
                    break;

                case "actualizare":
                    System.out.print("Introduceți numele locației de actualizat: ");
                    String updateName = userInput.readLine();
                    System.out.print("Latitudine: ");
                    String newLatitude = userInput.readLine();
                    System.out.print("Longitudine: ");
                    String newLongitude = userInput.readLine();

                    out.println(updateName);
                    out.println(newLatitude);
                    out.println(newLongitude);

                    String updateResponse = in.readLine();
                    System.out.println(updateResponse);
                    break;

                case "adaugaprognoza":
                    System.out.print("Introduceți numele locației pentru care doriți să adăugați prognoza: ");
                    String forecastLocation = userInput.readLine();
                    out.println(forecastLocation);

                    System.out.print("Introduceți data (YYYY-MM-DD): ");
                    String date = userInput.readLine();
                    System.out.print("Introduceți condiția meteo (e.g., Soare, Ploaie): ");
                    String condition = userInput.readLine();
                    System.out.print("Introduceți temperatura: ");
                    String temperature = userInput.readLine();

                    out.println(date);
                    out.println(condition);
                    out.println(temperature);

                    String forecastResponse = in.readLine();
                    System.out.println(forecastResponse);
                    break;

                default:
                    System.out.println("Comandă necunoscută. Încercați din nou.");
                    break;
            }
        }
    }
}
