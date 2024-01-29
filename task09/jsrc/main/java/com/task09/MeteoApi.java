package com.task09;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MeteoApi {
    public static String getWeatherForecast() throws IOException {
        String apiUrl = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();


        // Set request method
        connection.setRequestMethod("GET");

        // Get response code
        int responseCode = connection.getResponseCode();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                return response.toString();
            } else {
                System.out.println("Error fetching weather forecast. Response code: " + responseCode);
                return null;
            }
        } finally {
            // Disconnect to release resources
            connection.disconnect();
        }
    }
}
