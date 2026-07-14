package com.helios.platform.pulse.service;

import com.helios.platform.pulse.error.LogFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class NotificationService extends TelegramLongPollingBot {

    private final String botToken;
    private final Long defaultChatId;

    public NotificationService(
            @Value("${application.telegram.bot.token}") String botToken,
            @Value("${application.telegram.bot.chatId}") Long defaultChatId) {
        super(botToken);
        this.botToken = botToken;
        this.defaultChatId = defaultChatId;
    }

    @Override
    public String getBotUsername() {
        return "Caribbean_cbt_bot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Not implemented
    }

    @Async
    public void sendNotification(String msg) {
        SendMessage message = new SendMessage();
        message.setText("CBT_API V1.1\n" + msg);
        message.setChatId(defaultChatId);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            LogFile.writeLogError(e, false);
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e, () -> "");
        }
    }
}
