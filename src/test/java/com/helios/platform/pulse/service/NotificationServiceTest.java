package com.helios.platform.pulse.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Spy
    private NotificationService notificationService = new NotificationService("dummyToken", 12345L) {
        @Override
        public <T extends java.io.Serializable, Method extends BotApiMethod<T>> T execute(Method method) throws TelegramApiException {
            // Mock the execute method to avoid real network calls
            return null;
        }
    };

    @Test
    void testGetters() {
        assertEquals("dummyToken", notificationService.getBotToken());
        assertEquals("Caribbean_cbt_bot", notificationService.getBotUsername());
    }

    @Test
    void testOnUpdateReceived() {
        // Just verify it doesn't throw since it's not implemented
        assertDoesNotThrow(() -> notificationService.onUpdateReceived(null));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSendNotification_Success() throws Exception {
        notificationService.sendNotification("Test message");
        verify(notificationService, times(1)).execute(any(BotApiMethod.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSendNotification_ExceptionHandled() throws Exception {
        NotificationService throwingService = spy(new NotificationService("dummyToken", 12345L) {
            @Override
            public <T extends java.io.Serializable, Method extends BotApiMethod<T>> T execute(Method method) throws TelegramApiException {
                throw new TelegramApiException("Mocked error");
            }
        });

        // It should catch the exception and log it, without propagating it up.
        assertDoesNotThrow(() -> throwingService.sendNotification("Test message"));
        verify(throwingService, times(1)).execute(any(BotApiMethod.class));
    }
}
