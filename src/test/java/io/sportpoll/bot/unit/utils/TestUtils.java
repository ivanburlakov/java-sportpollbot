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
import static org.mockito.ArgumentMatchers.any;
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

    public static class TelegramMockSetup {
        private final TelegramClient telegramClient;
        private final Message mockMessage;

        private TelegramMockSetup(TelegramClient telegramClient, Message mockMessage) {
            this.telegramClient = telegramClient;
            this.mockMessage = mockMessage;
        }

        public static TelegramMockSetup createBasicMocks() throws Exception {
            TelegramClient telegramClient = mock(TelegramClient.class);
            Message mockMessage = mock(Message.class);
            when(mockMessage.getMessageId()).thenReturn(123);
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class)))
                .thenReturn(mockMessage);
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.polls.SendPoll.class)))
                .thenReturn(mockMessage);
            when(telegramClient
                .execute(any(org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText.class)))
                    .thenReturn(mockMessage);
            when(telegramClient
                .execute(any(org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage.class)))
                    .thenReturn(true);
            when(telegramClient
                .execute(any(org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage.class)))
                    .thenReturn(true);
            return new TelegramMockSetup(telegramClient, mockMessage);
        }

        public TelegramClient getTelegramClient() {
            return telegramClient;
        }

        public Message getMockMessage() {
            return mockMessage;
        }
    }

    public static class PollTestBuilder {
        private String question = "Test question";
        private String positiveOption = "Yes";
        private String negativeOption = "No";
        private int voteLimit = 5;
        private long chatId = -1001234567890L;
        private long userId = 123456789L;

        public PollTestBuilder withQuestion(String question) {
            this.question = question;
            return this;
        }

        public PollTestBuilder withOptions(String positive, String negative) {
            this.positiveOption = positive;
            this.negativeOption = negative;
            return this;
        }

        public PollTestBuilder withVoteLimit(int limit) {
            this.voteLimit = limit;
            return this;
        }

        public PollTestBuilder withChatId(long chatId) {
            this.chatId = chatId;
            return this;
        }

        public PollTestBuilder withUserId(long userId) {
            this.userId = userId;
            return this;
        }

        public void createPoll(PollManager pollManager) throws Exception {
            Update update = createMockUpdate("test", chatId, userId);
            pollManager.createAndPostPoll(question, positiveOption, negativeOption, voteLimit, update);
        }
    }

    public static PollTestBuilder pollBuilder() {
        return new PollTestBuilder();
    }

    public static void verifyNoInteractions(Object... mocks) {
        org.mockito.Mockito.verifyNoInteractions(mocks);
    }

    public static void verifyPollCreated(TelegramClient telegramClient) throws Exception {
        verify(telegramClient).execute(any(org.telegram.telegrambots.meta.api.methods.polls.SendPoll.class));
    }

    public static void verifyMessageSent(TelegramClient telegramClient) throws Exception {
        verify(telegramClient).execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class));
    }

    public static void verifyMessageEdited(TelegramClient telegramClient) throws Exception {
        verify(telegramClient)
            .execute(any(org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText.class));
    }

    public static void verifyPollPinned(TelegramClient telegramClient) throws Exception {
        verify(telegramClient)
            .execute(any(org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage.class));
    }

    public static void verifyPollUnpinned(TelegramClient telegramClient) throws Exception {
        verify(telegramClient)
            .execute(any(org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage.class));
    }

    public static class AdminSessionMockSetup {
        private final io.sportpoll.bot.config.WeeklyPollConfig weeklyConfig;
        private final io.sportpoll.bot.services.WeeklyPollScheduler weeklyScheduler;
        private final io.sportpoll.bot.persistance.DataStore dataStore;

        private AdminSessionMockSetup(io.sportpoll.bot.config.WeeklyPollConfig weeklyConfig,
            io.sportpoll.bot.services.WeeklyPollScheduler weeklyScheduler,
            io.sportpoll.bot.persistance.DataStore dataStore) {
            this.weeklyConfig = weeklyConfig;
            this.weeklyScheduler = weeklyScheduler;
            this.dataStore = dataStore;
        }

        public static AdminSessionMockSetup createDefaultMocks() {
            io.sportpoll.bot.config.WeeklyPollConfig weeklyConfig = mock(
                io.sportpoll.bot.config.WeeklyPollConfig.class);
            io.sportpoll.bot.services.WeeklyPollScheduler weeklyScheduler = mock(
                io.sportpoll.bot.services.WeeklyPollScheduler.class);
            io.sportpoll.bot.persistance.DataStore dataStore = mock(io.sportpoll.bot.persistance.DataStore.class);

            when(weeklyConfig.getQuestion()).thenReturn("Default question?");
            when(weeklyConfig.getPositiveOption()).thenReturn("Yes");
            when(weeklyConfig.getNegativeOption()).thenReturn("No");
            when(weeklyConfig.getTargetVotes()).thenReturn(10);
            when(weeklyConfig.getDayOfWeek()).thenReturn(java.time.DayOfWeek.MONDAY);
            when(weeklyConfig.getStartTime()).thenReturn(java.time.LocalTime.of(9, 0));
            when(weeklyScheduler.getConfig()).thenReturn(weeklyConfig);
            when(dataStore.get(io.sportpoll.bot.services.WeeklyPollScheduler.class)).thenReturn(weeklyScheduler);

            return new AdminSessionMockSetup(weeklyConfig, weeklyScheduler, dataStore);
        }

        public io.sportpoll.bot.config.WeeklyPollConfig getWeeklyConfig() {
            return weeklyConfig;
        }

        public io.sportpoll.bot.services.WeeklyPollScheduler getWeeklyScheduler() {
            return weeklyScheduler;
        }

        public io.sportpoll.bot.persistance.DataStore getDataStore() {
            return dataStore;
        }
    }

    public static void assertPollState(PollManager pollManager, boolean hasActivePoll, int expectedVotes) {
        org.junit.jupiter.api.Assertions.assertEquals(hasActivePoll, pollManager.hasActivePoll());
        if (hasActivePoll) {
            org.junit.jupiter.api.Assertions.assertEquals(expectedVotes, pollManager.getPositiveVotes());
        }
    }

    public static void assertNoErrors(Runnable testCode) {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(testCode::run);
    }

    public static void assertNoErrorsThrowable(ThrowableRunnable testCode) {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(testCode::run);
    }

    @FunctionalInterface
    public interface ThrowableRunnable {
        void run() throws Exception;
    }

    public static void executeWithTelegramMock(TelegramClientService.TelegramTestAction action) throws Exception {
        try (
            org.mockito.MockedStatic<io.sportpoll.bot.services.TelegramClientService> mockedStatic = org.mockito.Mockito
                .mockStatic(io.sportpoll.bot.services.TelegramClientService.class)) {
            TelegramMockSetup setup = TelegramMockSetup.createBasicMocks();
            mockedStatic.when(io.sportpoll.bot.services.TelegramClientService::getInstance)
                .thenReturn(setup.getTelegramClient());
            action.execute(setup);
        }
    }

    public static void executeWithDataStoreMock(DataStoreTestAction action) throws Exception {
        try (org.mockito.MockedStatic<io.sportpoll.bot.persistance.DataStore> mockedDataStore = org.mockito.Mockito
            .mockStatic(io.sportpoll.bot.persistance.DataStore.class)) {
            AdminSessionMockSetup setup = AdminSessionMockSetup.createDefaultMocks();
            mockedDataStore.when(io.sportpoll.bot.persistance.DataStore::getInstance).thenReturn(setup.getDataStore());
            action.execute(setup);
        }
    }

    @FunctionalInterface
    public interface DataStoreTestAction {
        void execute(AdminSessionMockSetup setup) throws Exception;
    }

    public static class TelegramClientService {
        @FunctionalInterface
        public interface TelegramTestAction {
            void execute(TelegramMockSetup setup) throws Exception;
        }
    }
}
