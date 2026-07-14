package com.helios.platform.pulse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helios.platform.pulse.entities.UserEntity;
import com.helios.platform.pulse.repositories.IUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TelegramService {

    @Value("${application.telegram.bot.token}")
    private String botToken;

    private final IUser userRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final ExecutorService executorService;

    private boolean isRunning = false;
    private long lastUpdateId = 0;
    private String botUsername = null;

    // Cache to hold generated tokens: UUID -> username
    private final ConcurrentHashMap<String, String> linkTokens = new ConcurrentHashMap<>();

    public TelegramService(IUser userRepository, NotificationService notificationService, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public String generateLinkToken(String username) {
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        linkTokens.put(token, username);
        return token;
    }

    public String getMagicLink(String username) {
        String token = generateLinkToken(username);
        // Si no hemos obtenido el username del bot aún, devolvemos un link genérico o pedimos buscarlo manualmente.
        if (botUsername == null) {
            return "https://t.me/EteccCbtBot?start=" + token; // Default fallback
        }
        return "https://t.me/" + botUsername + "?start=" + token;
    }

    public void sendMessage(String chatId, String message) {
        sendMessage(chatId, message, null, null);
    }

    public void sendMessage(String chatId, String message, String buttonText, String buttonUrl) {
        if (chatId == null || chatId.isEmpty()) return;

        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            // Format text properly for JSON
            String formattedMessage = message.replace("\"", "\\\"").replace("\n", "\\n");
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"chat_id\":\"").append(chatId).append("\",\"text\":\"").append(formattedMessage).append("\"");

            // Add inline keyboard if button is provided
            if (buttonText != null && !buttonText.isEmpty() && buttonUrl != null && !buttonUrl.isEmpty()) {
                jsonBuilder.append(",\"reply_markup\":{\"inline_keyboard\":[[{\"text\":\"")
                           .append(buttonText.replace("\"", "\\\""))
                           .append("\",\"url\":\"")
                           .append(buttonUrl.replace("\"", "\\\""))
                           .append("\"}]]}");
            }
            jsonBuilder.append("}");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBuilder.toString()))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() != 200) {
                            System.err.println("Telegram API Error: " + response.body());
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void startPolling() {
        if (botToken == null || botToken.isEmpty() || botToken.contains("dummy")) {
            System.out.println("Telegram Bot Token is dummy or empty. Polling disabled.");
            return;
        }

        isRunning = true;
        executorService.submit(() -> {
            // First, get the bot username
            try {
                String meUrl = "https://api.telegram.org/bot" + botToken + "/getMe";
                HttpRequest meReq = HttpRequest.newBuilder().uri(URI.create(meUrl)).GET().build();
                HttpResponse<String> meRes = httpClient.send(meReq, HttpResponse.BodyHandlers.ofString());
                if (meRes.statusCode() == 200) {
                    JsonNode meNode = objectMapper.readTree(meRes.body());
                    if (meNode.get("ok").asBoolean()) {
                        botUsername = meNode.get("result").get("username").asText();
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }

            while (isRunning) {
                try {
                    String url = "https://api.telegram.org/bot" + botToken + "/getUpdates?offset=" + lastUpdateId + "&timeout=30";
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .GET()
                            .timeout(java.time.Duration.ofSeconds(35))
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        JsonNode rootNode = objectMapper.readTree(response.body());
                        if (rootNode.get("ok").asBoolean()) {
                            JsonNode results = rootNode.get("result");
                            for (JsonNode update : results) {
                                long updateId = update.get("update_id").asLong();
                                lastUpdateId = updateId + 1; // Mark as read

                                if (update.has("message")) {
                                    JsonNode messageNode = update.get("message");
                                    if (messageNode.has("text")) {
                                        String text = messageNode.get("text").asText();
                                        String chatId = messageNode.get("chat").get("id").asText();

                                        handleIncomingMessage(chatId, text);
                                    }
                                }
                            }
                        }
                    }
                } catch (java.net.http.HttpTimeoutException te) {
                    // Normal timeout for long polling, ignore
                } catch (Exception e) {
                    try { Thread.sleep(5000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        });
    }

    private void handleIncomingMessage(String chatId, String text) {
        if (text.startsWith("/start ")) {
            String token = text.substring(7).trim();
            String username = linkTokens.get(token);
            if (username != null) {
                Optional<UserEntity> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    UserEntity user = userOpt.get();
                    user.setTelegramChatId(chatId); // This will be automatically encrypted by AesEncryptorConverter
                    userRepository.save(user);
                    linkTokens.remove(token);

                    sendMessage(chatId, "🎉 ¡Hola " + username + "! Qué gusto saludarte.\n\n" +
                                        "✅ Tu cuenta de Caribbean One Ecosystem V2 ha sido enlazada de forma segura y exitosa.\n\n" +
                                        "A partir de ahora, te enviaré tus códigos de verificación OTP por aquí de manera instantánea. ¡Cualquier cosa estoy para ayudarte!");
                    notificationService.sendNotification("Telegram linked for user " + username);
                } else {
                    sendMessage(chatId, "❌ Lo siento mucho, pero no logramos encontrar tu usuario en el ecosistema. Inténtalo de nuevo.");
                }
            } else {
                sendMessage(chatId, "⚠️ ¡Ups! Parece que este enlace mágico ya expiró o no es válido.\n\nNo te preocupes, puedes generar uno nuevecito desde tu panel en Caribbean One Ecosystem V2.");
            }
        } else {
            sendMessage(chatId, "👋 ¡Hola! Soy tu asistente oficial de seguridad de Caribbean One Ecosystem V2.\n\nPara vincular tu cuenta y comenzar a disfrutar de nuestras funciones, por favor utiliza el enlace mágico que encontrarás en tu perfil dentro de la aplicación. ¡Te espero!");
        }
    }

    @PreDestroy
    public void stopPolling() {
        isRunning = false;
        executorService.shutdownNow();
    }
}
