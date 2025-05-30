package com.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.example.util.Constant;

public class MyBot extends TelegramLongPollingBot {

    // Instance of CommandHandler to process incoming message
    private final CommandHandler commandHandler = new CommandHandler();

    @Override
    public String getBotUsername() {
        return Constant.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return Constant.BOT_TOKEN;
    }

    // Incoming messages go here
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            
            // Messages are handled by this
            commandHandler.handleMessage(message, this);
        }
    }

}
