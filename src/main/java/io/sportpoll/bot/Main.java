package io.sportpoll.bot;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import io.sportpoll.bot.persistance.DataStore;
import io.sportpoll.bot.config.Config;
import io.sportpoll.bot.constants.Messages;
import io.sportpoll.bot.services.TelegramClientService;

public class Main {
    public static void main(String[] args) {
        try {
            runBotInstance();
        } catch (Exception e) {
            System.err.println("Critical error in bot instance: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runBotInstance() throws Exception {
        DataStore.getInstance().setupShutdownHook();
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            Config config = Config.getInstance();
            SportPollBot bot = new SportPollBot(TelegramClientService.getInstance(), "SportPollBot");
            botsApplication.registerBot(config.botToken, bot);
            System.out.println(Messages.BOT_STARTED);
            System.out.println("Bot is ready and listening for updates...");
            Thread.currentThread().join();
        }
    }
}