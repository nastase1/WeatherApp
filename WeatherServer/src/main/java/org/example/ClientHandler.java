package org.example;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private WeatherDataLoader weatherDataLoader;
    private DatabaseManager databaseManager;


    public ClientHandler(Socket clientSocket, WeatherDataLoader weatherDataLoader, DatabaseManager databaseManager) {
        this.clientSocket = clientSocket;
        this.weatherDataLoader = weatherDataLoader;
        this.databaseManager = databaseManager;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            System.out.println("LOG DEBUG: Connection established with client: " + clientSocket.getRemoteSocketAddress());

            out.println("Welcome! Please enter your username:");
            String username = in.readLine();
            System.out.println("LOG DEBUG: Received username: " + username);

            boolean isAdmin = "admin".equals(username);
            System.out.println("LOG DEBUG: Is user admin: " + isAdmin);

            Optional<String> locationOpt = databaseManager.getLocationByUsername(username);
            System.out.println("LOG DEBUG: Retrieved location for user '" + username + "': " + locationOpt);

            if (!isAdmin && locationOpt.isEmpty()) {
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("Hello, ").append(username).append("!").append("Your current location is: unknown. Please enter a new location:");
                System.out.println("LOG DEBUG: Message sent is " + messageBuilder.toString());
                out.println(messageBuilder.toString());
                out.flush();

                String newLocation = in.readLine();
                System.out.println("LOG DEBUG: Received new location from user: " + newLocation);

                databaseManager.saveOrUpdateLocation(username, newLocation);
                System.out.println("LOG DEBUG: Updated location for user '" + username + "' to '" + newLocation + "' in database");

                messageBuilder.setLength(0);
                messageBuilder.append("Location updated successfully to %s!".formatted(newLocation));
                System.out.println("LOG DEBUG: Message sent is " + messageBuilder.toString());
                out.println(messageBuilder.toString());
                out.flush();
            } else if (!isAdmin) {
                String location = locationOpt.get();
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("Hello, ").append(username).append("! ")
                        .append("Your current location is: ").append(location);
                System.out.println("LOG DEBUG: Message sent is " + messageBuilder.toString());
                out.println(messageBuilder.toString());
            } else {
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("Hello, Admin! You have special privileges.");
                System.out.println("LOG DEBUG: Message sent is " + messageBuilder.toString());
                out.println(messageBuilder.toString());
            }

            while (true) {
                try {
                    String command = in.readLine();
                    if (command == null || command.strip().isEmpty()) {
                        System.out.println("LOG DEBUG: Client sent an empty or null command. Prompting again.");
                        out.println("Invalid command. Please enter a valid option.");
                        continue;
                    }

                    System.out.println("LOG DEBUG: Received command: " + command.strip());

                    switch (command.strip()) {
                        case "1":
                            System.out.println("LOG DEBUG: Option 1 selected - Get weather for location");
                            out.println("Enter location (or type 'current' for your current location):");

                            String locationQuery = in.readLine();
                            System.out.println("LOG DEBUG: Received location: " + locationQuery);
                            if (locationQuery == null || locationQuery.strip().isEmpty()) {
                                System.out.println("LOG DEBUG: Empty or null location query received.");
                                out.println("Invalid location. Please try again.");
                                break;
                            }

                            System.out.println("LOG DEBUG: Received location query: " + locationQuery.strip());
                            locationQuery = locationQuery.strip();

                            if ("current".equalsIgnoreCase(locationQuery)) {
                                if (isAdmin) {
                                    out.println("The Admin does not have a location");
                                    System.out.println("LOG DEBUG: Admin queried for 'current' location, which is not applicable");
                                    break;
                                } else {
                                    locationQuery = databaseManager.getLocationByUsername(username).orElse("unknown");
                                    System.out.println("LOG DEBUG: Resolved 'current' to user's location: " + locationQuery);
                                }
                            }

                            Optional<JsonObject> weatherOpt = weatherDataLoader.getWeatherData(locationQuery);
                            if (weatherOpt.isPresent()) {
                                JsonObject weather = weatherOpt.get();
                                System.out.println("LOG DEBUG: Weather data found for " + locationQuery + ": " + weather);
                                out.println("Weather data for " + locationQuery + ": " + weather.toString());
                            } else {
                                System.out.println("LOG DEBUG: No weather data found for " + locationQuery);
                                out.println("No weather data available for " + locationQuery);
                            }
                            break;

                        case "2":
                            System.out.println("LOG DEBUG: Option 2 selected - Set location");
                            if (isAdmin) {
                                out.println("Admin does not need a location.");
                                System.out.println("LOG DEBUG: Admin attempted to set a location, which is not allowed");
                            } else {
                                out.println("Enter new location:");

                                String newLocation = in.readLine();
                                if (newLocation == null || newLocation.strip().isEmpty()) {
                                    System.out.println("LOG DEBUG: Empty or null location received for setting.");
                                    out.println("Invalid location. Please try again.");
                                    break;
                                }

                                System.out.println("LOG DEBUG: Received new location to set: " + newLocation.strip());
                                newLocation = newLocation.strip();
                                databaseManager.saveOrUpdateLocation(username, newLocation);
                                System.out.println("LOG DEBUG: Updated location for user '" + username + "' to '" + newLocation + "' in database");
                                out.println("Location updated successfully to " + newLocation);
                            }
                            break;

                        case "3": // Admin: Add weather data
                            System.out.println("LOG DEBUG: Option 3 selected - Admin adds weather data");
                            if (isAdmin) {
                                out.println("Enter location for weather data:");
                                String location = in.readLine();
                                if (location == null || location.strip().isEmpty()) {
                                    System.out.println("LOG DEBUG: Empty or null location received for weather data.");
                                    out.println("Invalid location. Please try again.");
                                    break;
                                }

                                System.out.println("LOG DEBUG: Admin entered location for weather data: " + location.strip());
                                location = location.strip();

                                out.println("Enter weather data in JSON format:");
                                String weatherData = in.readLine();
                                if (weatherData == null || weatherData.strip().isEmpty()) {
                                    System.out.println("LOG DEBUG: Empty or null weather data received.");
                                    out.println("Invalid weather data. Please try again.");
                                    break;
                                }

                                System.out.println("LOG DEBUG: Admin entered weather data: " + weatherData.strip());
                                weatherData = weatherData.strip();

                                weatherDataLoader.addWeatherData(location, weatherData);
                                System.out.println("LOG DEBUG: Weather data added for location '" + location + "'");
                                out.println("Weather data added successfully for " + location);
                            } else {
                                out.println("Goodbye!");
                                System.out.println("LOG DEBUG: Non-admin user exited the application");
                                return;
                            }
                            break;

                        case "4": // Exit
                            System.out.println("LOG DEBUG: Option 4 selected - Exit");
                            out.println("Goodbye!");
                            try {
                                clientSocket.close();
                                System.out.println("LOG DEBUG: Client socket closed successfully");
                            } catch (IOException e) {
                                System.err.println("LOG ERROR: Failed to close client socket: " + e.getMessage());
                            }
                            return;

                        default:
                            System.out.println("LOG DEBUG: Unknown command: " + command.strip());
                            out.println("Invalid option. Please try again.");
                            break;
                    }
                } catch (IOException e) {
                    System.err.println("LOG ERROR: IOException occurred during communication: " + e.getMessage());
                    e.printStackTrace();
                    break; // Exit the loop if there's a communication error
                } catch (Exception e) {
                    System.err.println("LOG ERROR: Unexpected exception: " + e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}