package com.bubo.videoharvester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;

@Component public class HomeAssistantNotifier {

    private static final Logger logger = LoggerFactory.getLogger(HomeAssistantNotifier.class);

    @Value("${homeassistant.api.url}")
    private String apiUrl;

    @Value("${homeassistant.token}")
    private String token;

    @Value("${homeassistant.message.title}")
    private String title;

    public void sendNotification(String message) {

        if (token == null || token.isBlank()) {
            return;
        }
        HttpURLConnection con = null;

        try {
            URL url = new URL(apiUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + token);
            con.setRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setDoOutput(true);

            message = "[" + LocalTime.now().format(DateTimeFormatter.ISO_TIME) + "]" + message;

            String jsonInputString =
                    String.format("{\"title\": \"%s\", \"message\": \"%s\"}", escapeJson(title),
                            escapeJson(message));

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.debug("Notification sent successfully.");
            } else {
                logger.error("Failed to send notification. HTTP Response Code: {}", responseCode);
            }
        } catch (MalformedURLException e) {
            logger.error("Invalid URL: {}", e.getMessage(), e);
        } catch (IOException e) {
            logger.error("I/O Exception occurred: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage(), e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    public void sendNotification(String format, Object... arguments) {

        sendNotification(formatMessage(format, arguments));
    }

    private String formatMessage(String format, Object... args) {
        for (Object arg : args) {
            format = format.replaceFirst("\\{}", Matcher.quoteReplacement(String.valueOf(arg)));
        }
        return format;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\"", "\\\"").replace("\\", "\\\\").replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
