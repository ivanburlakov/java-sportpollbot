package io.sportpoll.bot.config;

import java.util.Arrays;
import java.util.List;

import io.sportpoll.bot.constants.Messages;

public class Config {
    public final String botToken;
    public final List<Long> adminUserIds;
    public final long targetGroupChatId;
    public final String logLevel;

    public Config(String botToken, List<Long> adminUserIds, long targetGroupChatId, String logLevel) {
        this.botToken = botToken;
        this.adminUserIds = adminUserIds;
        this.targetGroupChatId = targetGroupChatId;
        this.logLevel = logLevel;
    }

    private static volatile Config instance = null;

    public static Config getInstance() {
        if (instance == null) {
            synchronized (Config.class) {
                if (instance == null) {
                    instance = load();
                }
            }
        }
        return instance;
    }

    public static void setInstance(Config config) {
        instance = config;
    }

    private static Config load() {
        String botToken = getRequired("BOT_TOKEN");
        List<Long> adminUserIds = parseUserIds(getRequired("ADMIN_USER_IDS"));
        long targetGroupChatId = Long.parseLong(getRequired("TARGET_GROUP_CHAT_ID"));
        String logLevel = getOptional("LOG_LEVEL", "INFO");
        return new Config(botToken, adminUserIds, targetGroupChatId, logLevel);
    }

    private static String getRequired(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            value = System.getProperty(key);
        }
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(String.format(Messages.REQUIRED_ENV_VAR, key));
        }
        return value.trim();
    }

    private static String getOptional(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            value = System.getProperty(key);
        }
        return value != null && !value.trim().isEmpty() ? value.trim() : defaultValue;
    }

    private static List<Long> parseUserIds(String userIdsStr) {
        return Arrays.stream(userIdsStr.split(",")).map(String::trim).map(Long::parseLong).toList();
    }
}
