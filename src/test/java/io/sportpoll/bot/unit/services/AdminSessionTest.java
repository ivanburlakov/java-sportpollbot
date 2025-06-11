package io.sportpoll.bot.unit.services;

import io.sportpoll.bot.config.Config;
import io.sportpoll.bot.config.WeeklyPollConfig;
import io.sportpoll.bot.persistance.DataStore;
import io.sportpoll.bot.services.AdminSession;
import io.sportpoll.bot.services.PollManager;
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
        when(callbackQuery.getMessage()).thenReturn(callbackMessage);
        when(callbackQuery.getData()).thenReturn(data);
        return callbackQuery;
    }

    private void setupCallbackUpdate(String data) {
        CallbackQuery callbackQuery = createMockCallbackQuery(data);
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.hasMessage()).thenReturn(false);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
    }

    @Test
    void testHandleUpdateWithStartCommand() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            // Setup DataStore mock and weekly scheduler
            DataStore dataStore = mock(DataStore.class);
            mockedDataStore.when(DataStore::getInstance).thenReturn(dataStore);
            when(dataStore.get(WeeklyPollScheduler.class)).thenReturn(weeklyScheduler);
            // Mock poll manager state and Telegram API responses
            when(pollManager.hasActivePoll()).thenReturn(false);
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class)))
                .thenReturn(message);
            when(message.getMessageId()).thenReturn(123);
            // Create admin session and handle /start command
            adminSession = new AdminSession(update, telegramClient, pollManager);
            adminSession.handleUpdate(update);
            // Verify message was sent to user
            verify(telegramClient, atLeastOnce())
                .execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class));
        }
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
            when(update.hasCallbackQuery()).thenReturn(true);
            when(update.hasMessage()).thenReturn(false);
            when(update.getCallbackQuery()).thenReturn(callbackQuery);
            // Setup callback message context
            Message callbackMessage = mock(Message.class);
            when(callbackQuery.getMessage()).thenReturn(callbackMessage);
            when(callbackMessage.getChatId()).thenReturn(12345L);
            // Mock Telegram API responses
            when(pollManager.hasActivePoll()).thenReturn(false);
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class)))
                .thenReturn(message);
            when(message.getMessageId()).thenReturn(123);
            // Handle callback query update
            adminSession = new AdminSession(update, telegramClient, pollManager);
            adminSession.handleUpdate(update);
            // Verify response was sent
            verify(telegramClient, atLeastOnce())
                .execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class));
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
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            setupMockedDataStore(mockedDataStore);
            setupCallbackUpdate("main:close");

            adminSession = new AdminSession(update, telegramClient, pollManager);

            assertDoesNotThrow(() -> adminSession.handleUpdate(update));
            verify(pollManager).closeCurrentPoll(update);
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
    void testHandleWeeklyConfigToggleCallback() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            setupMockedDataStore(mockedDataStore);
            setupCallbackUpdate("weekly:config:toggle");

            adminSession = new AdminSession(update, telegramClient, pollManager);

            assertDoesNotThrow(() -> adminSession.handleUpdate(update));
        }
    }

    @Test
    void testHandleWeeklyDaySelectionCallback() throws TelegramApiException {
        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class)) {
            setupMockedDataStore(mockedDataStore);
            setupCallbackUpdate("weekly:day:3");

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

    private void setupMockedDataStore(MockedStatic<DataStore> mockedDataStore) {
        DataStore dataStore = mock(DataStore.class);
        mockedDataStore.when(DataStore::getInstance).thenReturn(dataStore);
        when(dataStore.get(WeeklyPollScheduler.class)).thenReturn(weeklyScheduler);
    }
}
