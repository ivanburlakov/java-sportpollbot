package io.sportpoll.bot.utils;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@FunctionalInterface
public interface TelegramAction {
    void execute() throws TelegramApiException;
}
