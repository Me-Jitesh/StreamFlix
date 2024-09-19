package com.jitesh.streamflix.utils;

import com.jitesh.streamflix.entities.Visitor;
import com.jitesh.streamflix.services.VisitorService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Scanner;

public interface IPLocation {
    static final String API_URL = "https://ipwhois.app/json/";

    public static Visitor getGeoLocation(String ipAddress) {
        try {
            String apiUrlWithIP = API_URL + ipAddress;
            URL url = new URL(apiUrlWithIP);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // Timeout for production use
            connection.setReadTimeout(5000);

            InputStream inputStream = connection.getInputStream();
            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
            String response = scanner.useDelimiter("\\A").next();
            JSONObject json = new JSONObject(response);

            // Map the JSON Response to the Model Class
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
                    Timestamp.from(Instant.now()));
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static Visitor extractIP(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            // Fallback to getRemoteAddr if X-Forwarded-For is not present
            clientIp = request.getRemoteAddr();
        } else {
            // If there are multiple IPs in X-Forwarded-For, take the first one
            clientIp = clientIp.split(",")[0];
        }
        return getGeoLocation(clientIp);
    }

    public static boolean isUniqueVisitor(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("isVisited")) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void setCookie(HttpServletResponse response, String ip) {
        Cookie cookie = new Cookie("isVisited", ip);
        cookie.setMaxAge(60 * 60 * 24 * 30);
        response.addCookie(cookie); // Add the cookie to the response
    }

    public static void saveVisitor(HttpServletRequest req, VisitorService visitorService, Visitor visitor) {
        // Visiting First Time
        if (req.getSession().getAttribute("visitorIP") == null) {
            visitorService.saveVisitor(visitor);
            req.getSession().setAttribute("visitorIP", visitor.getIp());
        }
    }
}