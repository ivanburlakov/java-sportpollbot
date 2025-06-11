package io.sportpoll.bot.functional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import io.sportpoll.bot.SportPollBot;
import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.WeeklyPollScheduler;
import io.sportpoll.bot.unit.utils.TestUtils;

import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;

public class UserPermissionsTest {

    @Mock
    private TelegramClient telegramClient;

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
        TestUtils.setupTestDatabaseDirectory("user-permissions");
    }

    @AfterEach
    void tearDown() {
        TestUtils.cleanupDatabaseFiles(originalUserDir, "UserPermissionsTest_");
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void testCompletePermissionEnforcementWorkflow() throws Exception {
        // Setup for permission testing
        PollManager realPollManager = new PollManager(-1001234567890L);
        SportPollBot bot = TestUtils.createTestBot(telegramClient,
            realPollManager,
            weeklyPollScheduler,
            scheduler,
            "UserPermissionsTest_" + testCounter + "_" + System.currentTimeMillis());

        // Test admin vs user permissions
        assertTrue(bot.isAdmin(123456789L)); // Admin user
        assertFalse(bot.isAdmin(999999999L)); // Regular user

        // Test user vs admin start command handling
        MessageContext userContext = TestUtils.createMockContext(999999999L, 987654321L, bot);
        MessageContext adminContext = TestUtils.createMockContext(123456789L, 987654321L, bot);

        // Both should execute without errors but with different behavior
        assertDoesNotThrow(() -> bot.start().action().accept(userContext));
        assertDoesNotThrow(() -> bot.start().action().accept(adminContext));
    }

    @Test
    void testUserStartCommandWorkflow() {
        // Create bot for user start command testing
        SportPollBot bot = TestUtils.createTestBot(telegramClient,
            new PollManager(-1001234567890L),
            weeklyPollScheduler,
            scheduler,
            "UserPermissionsTest_User_" + testCounter + "_" + System.currentTimeMillis());

        // Test non-admin start command in different contexts
        MessageContext userPrivateContext = TestUtils.createMockContext(987654321L, 111111111L, bot);
        MessageContext userGroupContext = TestUtils.createMockContext(987654321L, -1001234567890L, bot);

        // Execute user start commands
        assertDoesNotThrow(() -> bot.start().action().accept(userPrivateContext));
        assertDoesNotThrow(() -> bot.start().action().accept(userGroupContext));
    }
}
