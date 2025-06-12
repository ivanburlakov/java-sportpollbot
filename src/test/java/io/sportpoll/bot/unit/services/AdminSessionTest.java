package io.sportpoll.bot.unit.services;

import io.sportpoll.bot.config.Config;
import io.sportpoll.bot.config.WeeklyPollConfig;
import io.sportpoll.bot.persistance.DataStore;
import io.sportpoll.bot.services.AdminSession;
import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.TelegramClientService;
import io.sportpoll.bot.services.WeeklyPollScheduler;
import io.sportpoll.bot.unit.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminSessionTest {
    private Update update;
    private Message message;
    private TelegramClient telegramClient;
    private PollManager pollManager;
    private WeeklyPollScheduler weeklyScheduler;
    private WeeklyPollConfig weeklyConfig;
    private AdminSession adminSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Config.setInstance(TestUtils.createTestConfig());

        update = mock(Update.class);
        message = mock(Message.class);
        telegramClient = mock(TelegramClient.class);
        pollManager = mock(PollManager.class);
        weeklyScheduler = mock(WeeklyPollScheduler.class);
        weeklyConfig = mock(WeeklyPollConfig.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(12345L);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("/start");

        User user = mock(User.class);
        when(user.getId()).thenReturn(67890L);
        when(message.getFrom()).thenReturn(user);

        when(weeklyConfig.getQuestion()).thenReturn("Default question?");
        when(weeklyConfig.getPositiveOption()).thenReturn("Yes");
        when(weeklyConfig.getNegativeOption()).thenReturn("No");
        when(weeklyConfig.getTargetVotes()).thenReturn(10);
        when(weeklyConfig.getDayOfWeek()).thenReturn(java.time.DayOfWeek.MONDAY);
        when(weeklyConfig.getStartTime()).thenReturn(java.time.LocalTime.of(9, 0));
        when(weeklyScheduler.getConfig()).thenReturn(weeklyConfig);
    }

    private CallbackQuery createMockCallbackQuery(String data) {
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message callbackMessage = mock(Message.class);
        when(callbackMessage.getChatId()).thenReturn(12345L);
        when(callbackMessage.getMessageId()).thenReturn(123);
        when(callbackQuery.getMessage()).thenReturn(callbackMessage);
        when(callbackQuery.getData()).thenReturn(data);
        when(callbackQuery.getId()).thenReturn("callback_123");
        return callbackQuery;
    }

    private void setupCallbackUpdate(String data) {
        CallbackQuery callbackQuery = createMockCallbackQuery(data);
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.hasMessage()).thenReturn(false);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
    }

    @Test
    void testHandleUpdateWithStartCommand() throws Exception {
        TestUtils.executeWithDataStoreMock(setup -> {
            when(pollManager.hasActivePoll()).thenReturn(false);
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class)))
                .thenReturn(message);
            when(message.getMessageId()).thenReturn(123);

            adminSession = new AdminSession(update, telegramClient, pollManager);
            adminSession.handleUpdate(update);

            TestUtils.verifyMessageSent(telegramClient);
        });
    }

    @Test
    void testHandleUpdateWithCallbackQuery() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            // Setup DataStore and weekly scheduler mocks
            DataStore dataStore = mock(DataStore.class);
            mockedDataStore.when(DataStore::getInstance).thenReturn(dataStore);
            when(dataStore.get(WeeklyPollScheduler.class)).thenReturn(weeklyScheduler);
            // Create callback query for main menu
            CallbackQuery callbackQuery = mock(CallbackQuery.class);
            when(callbackQuery.getData()).thenReturn("main:menu");
            when(callbackQuery.getId()).thenReturn("callback_123");
            when(update.hasCallbackQuery()).thenReturn(true);
            when(update.hasMessage()).thenReturn(false);
            when(update.getCallbackQuery()).thenReturn(callbackQuery);
            // Setup callback message context
            Message callbackMessage = mock(Message.class);
            when(callbackQuery.getMessage()).thenReturn(callbackMessage);
            when(callbackMessage.getChatId()).thenReturn(12345L);
            when(callbackMessage.getMessageId()).thenReturn(456);
            // Mock Telegram API responses
            when(pollManager.hasActivePoll()).thenReturn(false);
            when(telegramClient
                .execute(any(org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText.class)))
                    .thenReturn(true);
            // Handle callback query update
            adminSession = new AdminSession(update, telegramClient, pollManager);
            adminSession.handleUpdate(update);
            // Verify response was sent (now expects EditMessageText instead of SendMessage)
            verify(telegramClient, atLeastOnce())
                .execute(any(org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText.class));
        }
    }

    @Test
    void testHandleUpdateWithNonStartMessage() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            // Setup DataStore and scheduler mocks
            DataStore dataStore = mock(DataStore.class);
            mockedDataStore.when(DataStore::getInstance).thenReturn(dataStore);
            when(dataStore.get(WeeklyPollScheduler.class)).thenReturn(weeklyScheduler);
            // Set message text to non-command text
            when(message.getText()).thenReturn("Some other text");
            when(update.hasCallbackQuery()).thenReturn(false);
            // Handle non-command message
            adminSession = new AdminSession(update, telegramClient, pollManager);
            adminSession.handleUpdate(update);
            // Verify no Telegram API calls made
            verifyNoInteractions(telegramClient);
        }
    }

    @Test
    void testHandleUpdateWithoutMessageOrCallback() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            // Setup DataStore and scheduler mocks
            DataStore dataStore = mock(DataStore.class);
            mockedDataStore.when(DataStore::getInstance).thenReturn(dataStore);
            when(dataStore.get(WeeklyPollScheduler.class)).thenReturn(weeklyScheduler);
            // Setup callback query update to avoid constructor NPE
            CallbackQuery callbackQuery = mock(CallbackQuery.class);
            Message callbackMessage = mock(Message.class);
            when(callbackQuery.getMessage()).thenReturn(callbackMessage);
            when(callbackMessage.getChatId()).thenReturn(12345L);
            when(update.hasCallbackQuery()).thenReturn(true);
            when(update.hasMessage()).thenReturn(false);
            when(update.getCallbackQuery()).thenReturn(callbackQuery);
            // Create admin session with valid constructor
            adminSession = new AdminSession(update, telegramClient, pollManager);
            // Test empty update with no message or callback
            Update emptyUpdate = mock(Update.class);
            when(emptyUpdate.hasCallbackQuery()).thenReturn(false);
            when(emptyUpdate.hasMessage()).thenReturn(false);
            // Handle empty update
            adminSession.handleUpdate(emptyUpdate);
            // Verify no API interactions occurred
            verifyNoInteractions(telegramClient);
        }
    }

    @Test
    void testHandleUpdateWithMessageWithoutText() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            DataStore dataStore = mock(DataStore.class);
            mockedDataStore.when(DataStore::getInstance).thenReturn(dataStore);
            when(dataStore.get(WeeklyPollScheduler.class)).thenReturn(weeklyScheduler);

            when(update.hasCallbackQuery()).thenReturn(false);
            when(update.hasMessage()).thenReturn(true);
            when(message.hasText()).thenReturn(false);

            adminSession = new AdminSession(update, telegramClient, pollManager);
            adminSession.handleUpdate(update);

            verifyNoInteractions(telegramClient);
        }
    }

    @Test
    void testHandleMainCreateCallback() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            setupMockedDataStore(mockedDataStore);
            setupCallbackUpdate("main:create");

            adminSession = new AdminSession(update, telegramClient, pollManager);

            assertDoesNotThrow(() -> adminSession.handleUpdate(update));
        }
    }

    @Test
    void testHandleMainWeeklyCallback() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            setupMockedDataStore(mockedDataStore);
            setupCallbackUpdate("main:weekly");

            adminSession = new AdminSession(update, telegramClient, pollManager);

            assertDoesNotThrow(() -> adminSession.handleUpdate(update));
        }
    }

    @Test
    void testHandleMainCloseCallback() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class);
            MockedStatic<TelegramClientService> mockedTelegramService = mockStatic(TelegramClientService.class)) {
            setupMockedDataStore(mockedDataStore);
            setupCallbackUpdate("main:close");

            // Mock TelegramClientService
            mockedTelegramService.when(TelegramClientService::getInstance).thenReturn(telegramClient);

            // Mock the poll manager to return true for successful close
            when(pollManager.closeCurrentPollSilent()).thenReturn(true);
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery.class)))
                .thenReturn(true);
            when(telegramClient
                .execute(any(org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText.class)))
                    .thenReturn(true);

            adminSession = new AdminSession(update, telegramClient, pollManager);

            assertDoesNotThrow(() -> adminSession.handleUpdate(update));
            verify(pollManager).closeCurrentPollSilent();
        }
    }

    @Test
    void testHandlePollEditQuestionCallback() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            setupMockedDataStore(mockedDataStore);

            adminSession = new AdminSession(update, telegramClient, pollManager);

            // First set up poll creation to initialize currentPollData
            setupCallbackUpdate("main:create");
            assertDoesNotThrow(() -> adminSession.handleUpdate(update));

            // Then test poll edit
            setupCallbackUpdate("poll:edit:question");
            assertDoesNotThrow(() -> adminSession.handleUpdate(update));
        }
    }

    @Test
    void testHandlePollMenuCallback() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            setupMockedDataStore(mockedDataStore);
            setupCallbackUpdate("poll:menu");

            adminSession = new AdminSession(update, telegramClient, pollManager);

            assertDoesNotThrow(() -> adminSession.handleUpdate(update));
        }
    }

    @Test
    void testHandleWeeklyMenuCallback() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            setupMockedDataStore(mockedDataStore);
            setupCallbackUpdate("weekly:menu");

            adminSession = new AdminSession(update, telegramClient, pollManager);

            assertDoesNotThrow(() -> adminSession.handleUpdate(update));
        }
    }

    @Test
    void testHandleEditTargetQuestion() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            setupMockedDataStore(mockedDataStore);

            adminSession = new AdminSession(update, telegramClient, pollManager);

            // First set up poll creation to initialize currentPollData
            setupCallbackUpdate("main:create");
            assertDoesNotThrow(() -> adminSession.handleUpdate(update));

            // Then simulate entering edit mode
            setupCallbackUpdate("poll:edit:question");
            assertDoesNotThrow(() -> adminSession.handleUpdate(update));

            // Now test text input handling
            when(update.hasCallbackQuery()).thenReturn(false);
            when(update.hasMessage()).thenReturn(true);
            when(message.hasText()).thenReturn(true);
            when(message.getText()).thenReturn("New question text");

            assertDoesNotThrow(() -> adminSession.handleUpdate(update));
        }
    }

    @Test
    void testHandleInvalidCallback() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            setupMockedDataStore(mockedDataStore);
            setupCallbackUpdate("invalid:callback");

            adminSession = new AdminSession(update, telegramClient, pollManager);

            assertDoesNotThrow(() -> adminSession.handleUpdate(update));
        }
    }

    @Test
    void testWeeklyConfigActualValueChanges() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            WeeklyPollConfig realConfig = new WeeklyPollConfig();
            realConfig.setQuestion("Original question");
            realConfig.setTargetVotes(5);
            realConfig.setEnabled(false);

            DataStore dataStore = mock(DataStore.class);
            mockedDataStore.when(DataStore::getInstance).thenReturn(dataStore);
            when(dataStore.get(WeeklyPollScheduler.class)).thenReturn(weeklyScheduler);
            when(weeklyScheduler.getConfig()).thenReturn(realConfig);

            adminSession = new AdminSession(update, telegramClient, pollManager);

            setupCallbackUpdate("weekly:config:toggle");
            adminSession.handleUpdate(update);

            assertTrue(realConfig.isEnabled(), "Config enabled state should change from false to true");

            setupCallbackUpdate("weekly:config:question");
            adminSession.handleUpdate(update);

            when(update.hasCallbackQuery()).thenReturn(false);
            when(update.hasMessage()).thenReturn(true);
            when(message.hasText()).thenReturn(true);
            when(message.getText()).thenReturn("New question text");

            adminSession.handleUpdate(update);

            assertEquals("New question text",
                realConfig.getQuestion(),
                "Config question should be updated to new value");
        }
    }

    @Test
    void testWeeklyConfigVotesTargetChange() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            WeeklyPollConfig realConfig = new WeeklyPollConfig();
            realConfig.setTargetVotes(10);

            DataStore dataStore = mock(DataStore.class);
            mockedDataStore.when(DataStore::getInstance).thenReturn(dataStore);
            when(dataStore.get(WeeklyPollScheduler.class)).thenReturn(weeklyScheduler);
            when(weeklyScheduler.getConfig()).thenReturn(realConfig);

            adminSession = new AdminSession(update, telegramClient, pollManager);

            setupCallbackUpdate("weekly:config:votes");
            adminSession.handleUpdate(update);

            when(update.hasCallbackQuery()).thenReturn(false);
            when(update.hasMessage()).thenReturn(true);
            when(message.hasText()).thenReturn(true);
            when(message.getText()).thenReturn("15");

            int originalVotes = realConfig.getTargetVotes();
            assertEquals(10, originalVotes, "Initial target votes should be 10");

            adminSession.handleUpdate(update);

            assertEquals(15, realConfig.getTargetVotes(), "Target votes should change from 10 to 15");
            verify(weeklyScheduler).updateConfig(realConfig);
        }
    }

    @Test
    void testWeeklyConfigDayChange() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            WeeklyPollConfig realConfig = new WeeklyPollConfig();
            realConfig.setDayOfWeek(java.time.DayOfWeek.MONDAY);

            DataStore dataStore = mock(DataStore.class);
            mockedDataStore.when(DataStore::getInstance).thenReturn(dataStore);
            when(dataStore.get(WeeklyPollScheduler.class)).thenReturn(weeklyScheduler);
            when(weeklyScheduler.getConfig()).thenReturn(realConfig);

            adminSession = new AdminSession(update, telegramClient, pollManager);

            java.time.DayOfWeek originalDay = realConfig.getDayOfWeek();
            assertEquals(java.time.DayOfWeek.MONDAY, originalDay, "Initial day should be Monday");

            setupCallbackUpdate("weekly:day:3");
            adminSession.handleUpdate(update);

            assertEquals(java.time.DayOfWeek.WEDNESDAY,
                realConfig.getDayOfWeek(),
                "Day should change from Monday to Wednesday");
            verify(weeklyScheduler).updateConfig(realConfig);
        }
    }

    @Test
    void testWeeklyConfigTimeChange() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            WeeklyPollConfig realConfig = new WeeklyPollConfig();
            realConfig.setStartTime(java.time.LocalTime.of(9, 0));

            DataStore dataStore = mock(DataStore.class);
            mockedDataStore.when(DataStore::getInstance).thenReturn(dataStore);
            when(dataStore.get(WeeklyPollScheduler.class)).thenReturn(weeklyScheduler);
            when(weeklyScheduler.getConfig()).thenReturn(realConfig);

            adminSession = new AdminSession(update, telegramClient, pollManager);

            java.time.LocalTime originalTime = realConfig.getStartTime();
            assertEquals(java.time.LocalTime.of(9, 0), originalTime, "Initial time should be 09:00");

            setupCallbackUpdate("weekly:config:time");
            adminSession.handleUpdate(update);

            when(update.hasCallbackQuery()).thenReturn(false);
            when(update.hasMessage()).thenReturn(true);
            when(message.hasText()).thenReturn(true);
            when(message.getText()).thenReturn("14");

            adminSession.handleUpdate(update);

            assertEquals(java.time.LocalTime.of(14, 0),
                realConfig.getStartTime(),
                "Time should change from 09:00 to 14:00");
            verify(weeklyScheduler).updateConfig(realConfig);
        }
    }

    @Test
    void testWeeklyConfigOptionsChange() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            WeeklyPollConfig realConfig = new WeeklyPollConfig();
            realConfig.setPositiveOption("Original Yes");
            realConfig.setNegativeOption("Original No");

            DataStore dataStore = mock(DataStore.class);
            mockedDataStore.when(DataStore::getInstance).thenReturn(dataStore);
            when(dataStore.get(WeeklyPollScheduler.class)).thenReturn(weeklyScheduler);
            when(weeklyScheduler.getConfig()).thenReturn(realConfig);

            adminSession = new AdminSession(update, telegramClient, pollManager);

            String originalPositive = realConfig.getPositiveOption();
            String originalNegative = realConfig.getNegativeOption();
            assertEquals("Original Yes", originalPositive, "Initial positive option should be 'Original Yes'");
            assertEquals("Original No", originalNegative, "Initial negative option should be 'Original No'");

            setupCallbackUpdate("weekly:config:positive");
            adminSession.handleUpdate(update);

            when(update.hasCallbackQuery()).thenReturn(false);
            when(update.hasMessage()).thenReturn(true);
            when(message.hasText()).thenReturn(true);
            when(message.getText()).thenReturn("Updated Yes");

            adminSession.handleUpdate(update);

            assertEquals("Updated Yes",
                realConfig.getPositiveOption(),
                "Positive option should change to 'Updated Yes'");
            verify(weeklyScheduler).updateConfig(realConfig);

            setupCallbackUpdate("weekly:config:negative");
            adminSession.handleUpdate(update);

            when(update.hasCallbackQuery()).thenReturn(false);
            when(update.hasMessage()).thenReturn(true);
            when(message.hasText()).thenReturn(true);
            when(message.getText()).thenReturn("Updated No");
            adminSession.handleUpdate(update);

            assertEquals("Updated No", realConfig.getNegativeOption(), "Negative option should change to 'Updated No'");
            verify(weeklyScheduler, times(2)).updateConfig(realConfig);
        }
    }

    private void setupMockedDataStore(MockedStatic<DataStore> mockedDataStore) {
        DataStore dataStore = mock(DataStore.class);
        mockedDataStore.when(DataStore::getInstance).thenReturn(dataStore);
        when(dataStore.get(WeeklyPollScheduler.class)).thenReturn(weeklyScheduler);
    }
}
