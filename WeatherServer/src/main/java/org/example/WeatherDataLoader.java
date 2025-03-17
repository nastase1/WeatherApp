package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class WeatherDataLoader {
    private Map<String, JsonObject> weatherData;
    private String dataFilePath;

    public WeatherDataLoader(String filePath) {
        weatherData = new HashMap<>();
        this.dataFilePath = filePath;
        loadWeatherData(filePath);
    }

    public void loadWeatherData(String filePath) {
        try (Reader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, JsonObject>>() {}.getType();
            weatherData = gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<JsonObject> getWeatherData(String location) {
        if (weatherData.containsKey(location)) {
            return Optional.of(weatherData.get(location));
        }
        return findClosestLocation(location);
    }

    private Optional<JsonObject> findClosestLocation(String location) {
        String closestLocation = null;
        int minDistance = Integer.MAX_VALUE;

        for (String key : weatherData.keySet()) {
            int distance = calculateDistance(location, key);
            if (distance < minDistance) {
                minDistance = distance;
                closestLocation = key;
            }
        }

        return closestLocation != null ? Optional.of(weatherData.get(closestLocation)) : Optional.empty();
    }


    private int calculateDistance(String a, String b) {
        return Math.abs(a.length() - b.length());
    }

    public void addWeatherData(String location, String weatherDataJson) {
        try {
            JsonObject newWeatherData = JsonParser.parseString(weatherDataJson).getAsJsonObject();
            weatherData.put(location, newWeatherData);
            saveWeatherDataToFile();
        } catch (JsonSyntaxException e) {
            System.err.println("Invalid JSON format for weather data: " + weatherDataJson);
        }
    }

    public void saveWeatherDataToFile() {
        try (FileWriter writer = new FileWriter(dataFilePath)) {
            Gson gson = new Gson();
            gson.toJson(weatherData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
