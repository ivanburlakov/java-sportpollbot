package io.sportpoll.bot.functional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import io.sportpoll.bot.SportPollBot;
import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.WeeklyPollScheduler;
import io.sportpoll.bot.unit.utils.TestUtils;

import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;

public class AdminFeaturesTest {

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private PollManager pollManager;

    @Mock
    private WeeklyPollScheduler weeklyPollScheduler;

    @Mock
    private ScheduledExecutorService scheduler;

    private static int testCounter = 0;
    private String originalUserDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        originalUserDir = System.getProperty("user.dir");
        TestUtils.setupTestDatabaseDirectory("admin-features");
        TestUtils.mockTelegramApi(telegramClient);
        io.sportpoll.bot.config.Config.setInstance(TestUtils.createTestConfig());
    }

    @AfterEach
    void tearDown() {
        TestUtils.cleanupDatabaseFiles(originalUserDir, "AdminFeaturesTest_");
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void testCompleteAdminWorkflow() throws Exception {
        // Create bot for admin testing
        SportPollBot bot = TestUtils.createTestBot(telegramClient,
            pollManager,
            weeklyPollScheduler,
            scheduler,
            "AdminFeaturesTest_" + testCounter + "_" + System.currentTimeMillis());

        // Test admin message handling workflow
        Update adminMessage = TestUtils.createMockUpdate("admin text", 123456789L, 123456789L);

        // Execute admin message handling
        assertDoesNotThrow(() -> bot.adminMessageReply().action().accept(bot, adminMessage));

        // Verify admin privileges work correctly
        assertTrue(bot.isAdmin(123456789L));
        assertFalse(bot.isAdmin(987654321L));
    }

    @Test
    void testAdminStartCommandWorkflow() {
        // Create bot for admin start command testing
        SportPollBot bot = TestUtils.createTestBot(telegramClient,
            pollManager,
            weeklyPollScheduler,
            scheduler,
            "AdminFeaturesTest_Start_" + testCounter + "_" + System.currentTimeMillis());

        // Test admin start command in different contexts
        MessageContext adminPrivateContext = TestUtils.createMockContext(123456789L, 987654321L, bot);
        MessageContext adminGroupContext = TestUtils.createMockContext(123456789L, -1001234567890L, bot);

        // Execute admin start commands
        assertDoesNotThrow(() -> bot.start().action().accept(adminPrivateContext));
        assertDoesNotThrow(() -> bot.start().action().accept(adminGroupContext));
    }
}
