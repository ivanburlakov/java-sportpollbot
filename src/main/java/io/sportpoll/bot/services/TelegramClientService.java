package io.sportpoll.bot.services;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import io.sportpoll.bot.config.Config;

public class TelegramClientService {
    private static volatile TelegramClient instance = null;

    public static TelegramClient getInstance() {
        if (instance == null) {
            synchronized (TelegramClientService.class) {
                if (instance == null) {
                    instance = new OkHttpTelegramClient(Config.getInstance().botToken);
                }
            }
        }
        return instance;
    }
}
