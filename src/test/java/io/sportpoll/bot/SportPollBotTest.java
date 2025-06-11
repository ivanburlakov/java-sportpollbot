package io.sportpoll.bot;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.WeeklyPollScheduler;
import io.sportpoll.bot.unit.utils.TestUtils;

import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;

public class SportPollBotTest {

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private PollManager pollManager;

    @Mock
    private WeeklyPollScheduler weeklyPollScheduler;

    @Mock
    private ScheduledExecutorService scheduler;

    private static int testCounter = 0;

    private SportPollBot createBot() {
        testCounter++;
        String uniqueBotName = "TestBot_" + testCounter + "_" + System.currentTimeMillis();
        return TestUtils.createTestBot(telegramClient, pollManager, weeklyPollScheduler, scheduler, uniqueBotName);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        TestUtils.mockTelegramApi(telegramClient);
    }

    @AfterEach
    void tearDown() {
        // No cleanup needed - database files are created in target directory
    }

    @Test
    void testCreatorId() {
        // Create bot instance
        SportPollBot bot = createBot();
        // Verify creator ID matches test configuration
        assertEquals(123456789L, bot.creatorId());
    }

    @Test
    void testIsAdmin() {
        // Create bot instance
        SportPollBot bot = createBot();
        // Verify admin privilege checking
        assertTrue(bot.isAdmin(123456789L));
        assertFalse(bot.isAdmin(987654321L));
    }
}
