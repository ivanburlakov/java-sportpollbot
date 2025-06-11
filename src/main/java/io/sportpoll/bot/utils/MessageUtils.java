package io.sportpoll.bot.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.Update;
import io.sportpoll.bot.config.Config;
import io.sportpoll.bot.constants.Messages;
import io.sportpoll.bot.services.AdminSession;
import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.TelegramClientService;

public class MessageUtils {
    private static final Map<String, AdminSession> adminSessions = new ConcurrentHashMap<>(); // Key is now String

    public static void routeUpdate(Update update, PollManager pollManager) throws TelegramApiException {
        if (update == null) return;
        if (update.hasPollAnswer()) {
            pollManager.handleDirectVote(update);
            return;
        }
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update, pollManager);
            return;
        }
        if (!update.hasMessage()) return;
        routeMessage(update, pollManager);
    }

    private static void routeMessage(Update update, PollManager pollManager) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        Config config = Config.getInstance();
        boolean isAdmin = config.adminUserIds.contains(userId);
        boolean isTargetGroup = (chatId == config.targetGroupChatId);
        if (isAdmin && !isTargetGroup) {
            handleAdminMessage(update, pollManager);
        } else if (isTargetGroup) {
            handleGroupMessage(update, pollManager);
        }
    }

    private static void handleGroupMessage(Update update, PollManager pollManager) throws TelegramApiException {
        String command = CommandUtils.parseCommand(update.getMessage().getText());
        switch (command) {
            case "/+" -> pollManager.handleVoteCommand(update);
            case "/-" -> pollManager.handleRevokeCommand(update);
        }
    }

    private static void handleAdminMessage(Update update, PollManager pollManager) throws TelegramApiException {
        long userId = update.getMessage().getFrom().getId();
        long chatId = update.getMessage().getChatId();
        String sessionKey = userId + ":" + chatId;
        String message = update.getMessage().getText();
        AdminSession session = adminSessions.computeIfAbsent(sessionKey, k -> new AdminSession(update, pollManager));
        if (CommandUtils.isCommand(message)) {
            if (CommandUtils.parseCommand(message).equals("/start")) {
                session.handleUpdate(update);
            }
            return;
        }
        session.handleUpdate(update);
    }

    private static void handleCallbackQuery(Update update, PollManager pollManager) throws TelegramApiException {
        long userId = update.getCallbackQuery().getFrom().getId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String sessionKey = userId + ":" + chatId;
        Config config = Config.getInstance();
        if (!config.adminUserIds.contains(userId)) {
            acknowledgeCallback(update, "");
            return;
        }
        AdminSession session = adminSessions.computeIfAbsent(sessionKey, k -> new AdminSession(update, pollManager));
        session.handleUpdate(update);
    }

    public static void sendMessage(String text, long chatId) throws TelegramApiException {
        SendMessage message = SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .parseMode("HTML")
            .disableNotification(true)
            .build();
        TelegramClientService.getInstance().execute(message);
    }

    public static void sendError(String text, long chatId) throws TelegramApiException {
        sendMessage(String.format(Messages.ERROR, text), chatId);
    }

    public static void acknowledgeCallback(Update update, String text) throws TelegramApiException {
        if (update.hasCallbackQuery()) {
            AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(update.getCallbackQuery().getId())
                .text(text)
                .showAlert(false)
                .build();
            TelegramClientService.getInstance().execute(answer);
        }
    }
}
