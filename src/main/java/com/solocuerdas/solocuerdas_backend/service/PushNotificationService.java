package com.solocuerdas.solocuerdas_backend.service;

import com.solocuerdas.solocuerdas_backend.model.Usuario;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * PUSH NOTIFICATION SERVICE
 * Sends push notifications to Expo clients via the Expo Push API.
 * https://docs.expo.dev/push-notifications/sending-notifications/
 *
 * Silently ignores send failures so that notification errors never break
 * the main business flow.
 */
@Service
public class PushNotificationService {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Send a push notification to a single user.
     * Does nothing if the user has no registered push token.
     */
    public void send(Usuario recipient, String title, String body) {
        String token = recipient.getExpoPushToken();
        if (token == null || token.isBlank()) {
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("Accept-Encoding", "gzip, deflate");

            Map<String, Object> payload = new HashMap<>();
            payload.put("to", token);
            payload.put("title", title);
            payload.put("body", body);
            payload.put("sound", "default");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForObject(EXPO_PUSH_URL, request, String.class);
        } catch (Exception e) {
            // Log silently — push failures must never break the main flow
            System.err.println("[PushNotification] Failed to send to " + token + ": " + e.getMessage());
        }
    }
}
