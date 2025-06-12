package io.sportpoll.bot.unit.services;

import io.sportpoll.bot.config.Config;
import io.sportpoll.bot.config.WeeklyPollConfig;
import io.sportpoll.bot.persistance.DataStore;
import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.TelegramClientService;
import io.sportpoll.bot.services.WeeklyPollScheduler;
import io.sportpoll.bot.unit.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Simplified tests for WeeklyPollScheduler focusing on: 1. Window-based timing
 * (1 second before/after planned time) 2. Monday reset and reschedule scenarios
 */
class WeeklyPollSchedulerTest {
    private WeeklyPollConfig config;
    private WeeklyPollScheduler scheduler;
    private AtomicReference<LocalDateTime> currentTime;
    private ScheduledExecutorService testExecutor;

    @BeforeEach
    void setUp() {
        Config.setInstance(TestUtils.createTestConfig());
        config = mock(WeeklyPollConfig.class);
        currentTime = new AtomicReference<>(LocalDateTime.of(2024, 6, 10, 12, 0)); // Monday 12:00
        testExecutor = Executors.newScheduledThreadPool(2);

        when(config.getQuestion()).thenReturn("Weekly test question?");
        when(config.getPositiveOption()).thenReturn("Yes");
        when(config.getNegativeOption()).thenReturn("No");
        when(config.getTargetVotes()).thenReturn(10);
        when(config.getDayOfWeek()).thenReturn(DayOfWeek.MONDAY);
        when(config.getStartTime()).thenReturn(LocalTime.of(14, 0));
        when(config.isEnabled()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        if (testExecutor != null && !testExecutor.isShutdown()) {
            testExecutor.shutdown();
        }
    }

    @Test
    void testWindowTimingOneSecondBefore() throws TelegramApiException {
        PollManager realPollManager = new PollManager(-1001234567890L);
        LocalDateTime scheduledTime = LocalDateTime.of(2024, 6, 10, 14, 0);
        LocalDateTime oneSecondBefore = scheduledTime.minusSeconds(1);
        currentTime.set(oneSecondBefore);

        scheduler = new WeeklyPollScheduler(config, realPollManager, currentTime::get, () -> 0.0, testExecutor);
        scheduler.setRandomWindow(Duration.ofMinutes(5)); // 5-minute window

        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class);
            MockedStatic<TelegramClientService> mockedTelegram = mockStatic(TelegramClientService.class)) {

            setupMocks(mockedDataStore, mockedTelegram, realPollManager);

            scheduler.initialize();
            scheduler.runScheduledPollCheck();

            // Should NOT create poll - 1 second before window start
            assertFalse(realPollManager.hasActivePoll());
        }
    }

    @Test
    void testWindowTimingOneSecondAfter() throws TelegramApiException {
        PollManager realPollManager = new PollManager(-1001234567890L);
        LocalDateTime scheduledTime = LocalDateTime.of(2024, 6, 10, 14, 0);
        LocalDateTime oneSecondAfter = scheduledTime.plusSeconds(1);
        currentTime.set(oneSecondAfter);

        scheduler = new WeeklyPollScheduler(config, realPollManager, currentTime::get, () -> 0.0, testExecutor);
        scheduler.setRandomWindow(Duration.ofMinutes(5)); // 5-minute window

        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class);
            MockedStatic<TelegramClientService> mockedTelegram = mockStatic(TelegramClientService.class)) {

            setupMocks(mockedDataStore, mockedTelegram, realPollManager);

            scheduler.initialize();
            scheduler.runScheduledPollCheck();

            // Should create poll - 1 second after window start
            assertTrue(realPollManager.hasActivePoll());
        }
    }

    @Test
    void testMondayResetAndReschedule() throws TelegramApiException {
        PollManager realPollManager = new PollManager(-1001234567890L);
        LocalDateTime pollTime = LocalDateTime.of(2024, 6, 10, 14, 0);
        currentTime.set(pollTime);

        scheduler = new WeeklyPollScheduler(config, realPollManager, currentTime::get, () -> 0.0, testExecutor);
        scheduler.setRandomWindow(Duration.ofMinutes(5));

        try (MockedStatic<DataStore> mockedDataStore = mockStatic(DataStore.class);
            MockedStatic<TelegramClientService> mockedTelegram = mockStatic(TelegramClientService.class)) {

            setupMocks(mockedDataStore, mockedTelegram, realPollManager);

            // Create initial poll
            Update createUpdate = TestUtils.createMockUpdate("test", -1001234567890L, 123456789L);
            realPollManager.createAndPostPoll("Test Poll", "Yes", "No", 10, createUpdate);
            assertTrue(realPollManager.hasActivePoll());

            // Execute Monday reset
            realPollManager.checkMondayClose();
            assertFalse(realPollManager.hasActivePoll());

            // Scheduler should be able to create new poll for current week
            scheduler.initialize();
            scheduler.runScheduledPollCheck();
            assertTrue(realPollManager.hasActivePoll());
        }
    }

    @Test
    void testConfigChangeTriggersReschedule() {
        PollManager realPollManager = new PollManager(-1001234567890L);
        LocalDateTime mondayMorning = LocalDateTime.of(2024, 6, 10, 10, 0);
        currentTime.set(mondayMorning);

        scheduler = new WeeklyPollScheduler(config, realPollManager, currentTime::get, () -> 0.0, testExecutor);

        // Initial schedule for Wednesday 16:00
        when(config.getDayOfWeek()).thenReturn(DayOfWeek.WEDNESDAY);
        when(config.getStartTime()).thenReturn(LocalTime.of(16, 0));

        LocalDateTime initialSchedule = scheduler.calculateNextPollTime(mondayMorning);
        assertEquals(DayOfWeek.WEDNESDAY, initialSchedule.getDayOfWeek());
        assertEquals(16, initialSchedule.getHour());

        // Change config to Tuesday 14:00
        when(config.getDayOfWeek()).thenReturn(DayOfWeek.TUESDAY);
        when(config.getStartTime()).thenReturn(LocalTime.of(14, 0));

        LocalDateTime rescheduledTime = scheduler.calculateNextPollTime(mondayMorning);
        assertEquals(DayOfWeek.TUESDAY, rescheduledTime.getDayOfWeek());
        assertEquals(14, rescheduledTime.getHour());
    }

    private void setupMocks(MockedStatic<DataStore> mockedDataStore, MockedStatic<TelegramClientService> mockedTelegram,
        PollManager realPollManager) throws TelegramApiException {
        DataStore dataStore = mock(DataStore.class);
        TelegramClient telegramClient = mock(TelegramClient.class);
        Message mockMessage = mock(Message.class);

        mockedDataStore.when(DataStore::getInstance).thenReturn(dataStore);
        mockedTelegram.when(TelegramClientService::getInstance).thenReturn(telegramClient);
        when(dataStore.get(PollManager.class)).thenReturn(realPollManager);
        when(dataStore.get(WeeklyPollConfig.class)).thenReturn(config);
        when(mockMessage.getMessageId()).thenReturn(12345);
        when(telegramClient.execute(any(SendPoll.class))).thenReturn(mockMessage);
        when(telegramClient.execute(any(SendMessage.class))).thenReturn(mockMessage);
        when(telegramClient.execute(any(PinChatMessage.class))).thenReturn(true);
    }
}