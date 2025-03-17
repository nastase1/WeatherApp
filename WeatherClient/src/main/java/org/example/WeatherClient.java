package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class WeatherClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the Weather Server");

            System.out.println(in.readLine());
            String username = scanner.nextLine();
            out.println(username);

            String serverResponse = in.readLine();
            System.out.println(serverResponse);

            boolean isAdmin = "admin".equals(username);

            if (!isAdmin && serverResponse.contains("Please enter a new location:")) {
                String newLocation = scanner.nextLine();
                out.println(newLocation);
                System.out.println(in.readLine());
            }
            System.out.println("\nMenu:");
            System.out.println("1. Get weather for location");
            System.out.println("2. Set location");
            if ("admin".equals(username)) {
                System.out.println("3. Add weather data");
            }
            System.out.println(isAdmin ? "4. Exit" : "3. Exit");
            System.out.print("Choose an option:");

            String choice = "";

            while (true) {
                choice = scanner.nextLine();
                out.println(choice);
                switch (choice) {
                    case "1":
                        System.out.println(in.readLine());
                        String locationChoice = scanner.nextLine();
                        System.out.println("LOG DEBUG: Location chosen %s".formatted(locationChoice));
                        out.println(locationChoice);
                        System.out.println("Server response: " + in.readLine());
                        break;

                    case "2":
                        if (!"admin".equals(username)) {
                            System.out.println(in.readLine());
                            String newLocation = scanner.nextLine();
                            out.println(newLocation);
                            System.out.println("Server response: " + in.readLine());
                            break;
                        }
                        else{
                            System.out.println(in.readLine());
                            break;
                        }

                    case "3":
                        if ("admin".equals(username)) {
                            System.out.println(in.readLine());
                            String adminLocation = scanner.nextLine();
                            out.println(adminLocation);

                            System.out.println(in.readLine());
                            String weatherData = scanner.nextLine();
                            out.println(weatherData);

                            System.out.println("Server response: " + in.readLine());
                            break;
                        }
                        else {
                            System.out.println(in.readLine());
                            return;
                        }

                    case "4": // Exit
                        System.out.println(in.readLine());
                        return;

                    default:
                        System.out.println("Invalid option. Please try again.");
                        break;
                }

                System.out.println("Choose an option:");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
