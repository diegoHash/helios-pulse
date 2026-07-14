package com.helios.platform.pulse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    @Value("${whatsapp.api.token:}")
    private String apiToken;

    @Value("${whatsapp.phone.id:}")
    private String phoneId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendBraceletNotification(String phoneNumber, String ownerName, int quantity, String braceletType, String propertyNum, int remaining) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            logger.warn("No phone number provided for property {}. Skipping WhatsApp notification.", propertyNum);
            return;
        }

        if (apiToken.isBlank() || phoneId.isBlank()) {
            logger.warn("WhatsApp notification skipped because the integration is not configured.");
            return;
        }

        // Clean phone number (remove +, spaces, dashes, etc.)
        String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");

        // Build WhatsApp Graph API URL
        String url = "https://graph.facebook.com/v19.0/" + phoneId + "/messages";

        Map<String, Object> templateObj = new HashMap<>();
        templateObj.put("name", "caribbean_one_notificacion");

        Map<String, String> langObj = new HashMap<>();
        langObj.put("code", "es"); // Código de idioma de la plantilla
        templateObj.put("language", langObj);

        // Parámetros de la plantilla
        java.util.List<Map<String, String>> parametersList = new java.util.ArrayList<>();

        parametersList.add(Map.of("type", "text", "parameter_name", "nombre", "text", ownerName != null ? ownerName : "Propietario"));
        parametersList.add(Map.of("type", "text", "parameter_name", "cantidad", "text", String.valueOf(quantity)));
        parametersList.add(Map.of("type", "text", "parameter_name", "tipo", "text", braceletType != null ? braceletType : "N/A"));
        parametersList.add(Map.of("type", "text", "parameter_name", "inmueble", "text", propertyNum != null ? propertyNum : "N/A"));
        parametersList.add(Map.of("type", "text", "parameter_name", "cupo_restante", "text", String.valueOf(remaining)));

        Map<String, Object> bodyComponent = new HashMap<>();
        bodyComponent.put("type", "body");
        bodyComponent.put("parameters", parametersList);

        // Componente Header requerido por la plantilla
        Map<String, Object> headerComponent = new HashMap<>();
        headerComponent.put("type", "header");
        headerComponent.put("parameters", java.util.Collections.singletonList(
            Map.of(
                "type", "image",
                "image", Map.of("link", "https://i.postimg.cc/Vk9vZDp0/IMG-6184.png")
            )
        ));

        templateObj.put("components", java.util.Arrays.asList(headerComponent, bodyComponent));

        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("recipient_type", "individual");
        payload.put("to", cleanPhone);
        payload.put("type", "template");
        payload.put("template", templateObj);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            logger.info("Sending WhatsApp message to {}", cleanPhone);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            logger.info("WhatsApp API response: {}", response.getBody());
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            logger.error("Failed to send WhatsApp message to {}. Status: {}. Error Body: {}", cleanPhone, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Failed to send WhatsApp message to {}: {}", cleanPhone, e.getMessage());
        }
    }
}
