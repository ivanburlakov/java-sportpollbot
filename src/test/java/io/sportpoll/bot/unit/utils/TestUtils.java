package io.sportpoll.bot.unit.utils;

import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import io.sportpoll.bot.SportPollBot;
import io.sportpoll.bot.config.Config;
import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.WeeklyPollScheduler;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.io.File;
import static org.mockito.Mockito.*;

public class TestUtils {
    private static User cachedUser = null;

    public static Config createTestConfig() {
        return new Config("test_token", List.of(123456789L), -1001234567890L, "INFO");
    }

    public static SportPollBot createTestBot(TelegramClient telegramClient, PollManager pollManager,
        WeeklyPollScheduler weeklyPollScheduler, ScheduledExecutorService scheduler, String uniqueName) {
        return new SportPollBot(telegramClient, uniqueName, pollManager, weeklyPollScheduler, createTestConfig(),
            scheduler);
    }

    public static Update createMockUpdate(String text, long chatId, long userId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User user = mock(User.class);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn(text);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        return update;
    }

    public static Update createMockCallbackUpdate(String callbackData, long chatId, long userId) {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);
        User user = mock(User.class);
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(callbackQuery.getFrom()).thenReturn(user);
        when(callbackQuery.getId()).thenReturn("callback_123");
        when(message.getChatId()).thenReturn(chatId);
        when(message.getMessageId()).thenReturn(12345);
        when(user.getId()).thenReturn(userId);
        when(user.getFirstName()).thenReturn("TestUser");
        return update;
    }

    public static Update createMockCallbackUpdate(String callbackData, long userId) {
        return createMockCallbackUpdate(callbackData, userId, userId);
    }

    public static void mockTelegramApi(TelegramClient client) {
    }

    public static MessageContext createMockContext(long userId, long chatId, SportPollBot bot) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getFirstName()).thenReturn("TestUser");
        Update update = mock(Update.class);
        return MessageContext.newContext(update, user, chatId, bot);
    }

    public static void setupTestDatabaseDirectory(String testName) {
        String originalUserDir = System.getProperty("user.dir");
        File testDbDir = new File(originalUserDir + "/target/test-db-" + testName);
        if (!testDbDir.exists()) {
            testDbDir.mkdirs();
        }
        System.setProperty("user.dir", testDbDir.getAbsolutePath());
    }

    public static void cleanupDatabaseFiles(String originalUserDir, String testPrefix) {
        try {
            File projectRoot = new File(originalUserDir);
            File testDbDir = new File(originalUserDir + "/target/test-db-" + testPrefix);
            File[] dbFiles = projectRoot.listFiles(
                (dir, name) -> name.startsWith(testPrefix) || (name.endsWith(".wal.0") && name.contains(testPrefix)));
            if (dbFiles != null) {
                for (File file : dbFiles) {
                    File target = new File(testDbDir, file.getName());
                    file.renameTo(target);
                }
            }
        } catch (Exception e) {
        }
    }

    public static Update createMockDirectVoteUpdate(long userId, String firstName, int optionId) {
        Update update = mock(Update.class);
        PollAnswer pollAnswer = mock(PollAnswer.class);
        User user = mock(User.class);

        when(update.hasPollAnswer()).thenReturn(true);
        when(update.getPollAnswer()).thenReturn(pollAnswer);
        when(pollAnswer.getUser()).thenReturn(user);
        when(pollAnswer.getOptionIds()).thenReturn(List.of(optionId));
        when(user.getId()).thenReturn(userId);
        when(user.getFirstName()).thenReturn(firstName);

        cachedUser = user;

        return update;
    }

    public static Update createMockDirectVoteUpdate(long userId, String firstName) {
        Update update = mock(Update.class);
        PollAnswer pollAnswer = mock(PollAnswer.class);
        User user = cachedUser != null && cachedUser.getId().equals(userId) ? cachedUser : mock(User.class);

        when(update.hasPollAnswer()).thenReturn(true);
        when(update.getPollAnswer()).thenReturn(pollAnswer);
        when(pollAnswer.getUser()).thenReturn(user);
        when(pollAnswer.getOptionIds()).thenReturn(List.of());
        when(user.getId()).thenReturn(userId);
        when(user.getFirstName()).thenReturn(firstName);

        return update;
    }
}
