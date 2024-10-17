package com.bubo.videoharvester;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("'['HH:mm:ss']'");

    private static final ObjectMapper objectMapper = new ObjectMapper();

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

            message = TIME_FORMATTER.format(LocalTime.now()) + message;

            HomeAssistantNotification notification = HomeAssistantNotification.builder()
                    .message(message)
                    .title(title)
                    .data(HomeAssistantNotification.Data.builder().ttl(0).priority("high").build())
                    .build();

            String jsonInputString = objectMapper.writeValueAsString(notification);

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
}
