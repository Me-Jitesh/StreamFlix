package com.jitesh.streamflix.utils;

import com.jitesh.streamflix.entities.Visitor;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Scanner;

public class IPLocation {
    private static final String API_URL = "https://ipwhois.app/json/";

    public static void main(String[] args) {
        Visitor visitor = getGeoLocation("152.59.28.191");
        System.out.println(visitor);
    }

    public static Visitor getGeoLocation(String ipAddress) {
        try {
            String apiUrlWithIP = API_URL + ipAddress;
            URL url = new URL(apiUrlWithIP);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);  // Timeout for production use
            connection.setReadTimeout(5000);

            InputStream inputStream = connection.getInputStream();
            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
            String response = scanner.useDelimiter("\\A").next();
            JSONObject json = new JSONObject(response);

            // Map the JSON Response to the  Model Class
            return new Visitor(null,
                    json.getString("ip"),
                    json.getString("country"),
                    json.getString("country_phone"),
                    json.getString("region"),
                    json.getString("city"),
                    json.getString("continent"),
                    json.getDouble("longitude"),
                    json.getDouble("latitude"),
                    json.getString("isp"),
                    json.getString("country_flag"),
                    json.getString("timezone"),
                    Timestamp.from(Instant.now())
            );
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}