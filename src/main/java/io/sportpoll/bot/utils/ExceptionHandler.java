package io.sportpoll.bot.utils;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ExceptionHandler {
    public static void handle(TelegramAction action) {
        try {
            action.execute();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
