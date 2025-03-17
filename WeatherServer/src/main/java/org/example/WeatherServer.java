package org.example;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WeatherServer {

    public static void main(String[] args) {
        int port = 12345;
        WeatherDataLoader weatherDataLoader = new WeatherDataLoader("src/main/resources/weather-data.json");
        weatherDataLoader.loadWeatherData("src/main/resources/weather-data.json");

        DatabaseManager databaseManager = new DatabaseManager();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Weather Server is running on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket, weatherDataLoader, databaseManager)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
