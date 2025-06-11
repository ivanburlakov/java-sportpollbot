package io.sportpoll.bot.routing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import io.sportpoll.bot.SportPollBot;
import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.WeeklyPollScheduler;
import io.sportpoll.bot.unit.utils.TestUtils;

import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;

public class CommandRoutingTest {

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

    private SportPollBot createBot() {
        testCounter++;
        String uniqueBotName = "CommandRoutingTest_" + testCounter + "_" + System.currentTimeMillis();
        return TestUtils.createTestBot(telegramClient, pollManager, weeklyPollScheduler, scheduler, uniqueBotName);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        originalUserDir = System.getProperty("user.dir");
        TestUtils.setupTestDatabaseDirectory("command-routing");
    }

    @AfterEach
    void tearDown() {
        TestUtils.cleanupDatabaseFiles(originalUserDir, "CommandRoutingTest_");
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void testVoteCommandRouting() throws TelegramApiException {
        SportPollBot bot = createBot();
        Reply voteReply = bot.voteReply();

        // Valid vote commands - both spaced and non-spaced formats
        Update basicVote = TestUtils.createMockUpdate("/+", -1001234567890L, 123456789L);
        Update numberedVote = TestUtils.createMockUpdate("/+1", -1001234567890L, 123456789L);
        Update numberedVoteSpaced = TestUtils.createMockUpdate("/+ 1", -1001234567890L, 123456789L);
        Update namedVote = TestUtils.createMockUpdate("/+ Alice", -1001234567890L, 123456789L);
        Update namedVoteNoSpace = TestUtils.createMockUpdate("/+Alice", -1001234567890L, 123456789L);

        // Invalid contexts
        Update wrongChat = TestUtils.createMockUpdate("/+1", 999999999L, 123456789L);
        Update nonVoteCommand = TestUtils.createMockUpdate("/help", -1001234567890L, 123456789L);

        // Verify correct routing for both formats
        assertTrue(voteReply.isOkFor(basicVote));
        assertTrue(voteReply.isOkFor(numberedVote));
        assertTrue(voteReply.isOkFor(numberedVoteSpaced));
        assertTrue(voteReply.isOkFor(namedVote));
        assertTrue(voteReply.isOkFor(namedVoteNoSpace));
        assertFalse(voteReply.isOkFor(wrongChat));
        assertFalse(voteReply.isOkFor(nonVoteCommand));
    }

    @Test
    void testRevokeCommandRouting() throws TelegramApiException {
        SportPollBot bot = createBot();
        Reply revokeReply = bot.revokeReply();

        // Valid revoke commands - both spaced and non-spaced formats
        Update basicRevoke = TestUtils.createMockUpdate("/-", -1001234567890L, 123456789L);
        Update numberedRevoke = TestUtils.createMockUpdate("/-2", -1001234567890L, 123456789L);
        Update numberedRevokeSpaced = TestUtils.createMockUpdate("/- 2", -1001234567890L, 123456789L);

        // Invalid contexts
        Update wrongChat = TestUtils.createMockUpdate("/-", 999999999L, 123456789L);
        Update voteCommand = TestUtils.createMockUpdate("/+1", -1001234567890L, 123456789L);

        // Verify correct routing for both formats
        assertTrue(revokeReply.isOkFor(basicRevoke));
        assertTrue(revokeReply.isOkFor(numberedRevoke));
        assertTrue(revokeReply.isOkFor(numberedRevokeSpaced));
        assertFalse(revokeReply.isOkFor(wrongChat));
        assertFalse(revokeReply.isOkFor(voteCommand));
    }

    @Test
    void testAdminMessageRouting() {
        SportPollBot bot = createBot();
        Reply adminReply = bot.adminMessageReply();

        // Valid admin messages
        Update adminMessage = TestUtils.createMockUpdate("admin text", 123456789L, 123456789L);

        // Invalid contexts
        Update nonAdminMessage = TestUtils.createMockUpdate("user text", 987654321L, 987654321L);
        Update commandMessage = TestUtils.createMockUpdate("/command", 123456789L, 123456789L);
        Update groupMessage = TestUtils.createMockUpdate("text", -1001234567890L, 123456789L);

        // Verify correct routing
        assertTrue(adminReply.isOkFor(adminMessage));
        assertFalse(adminReply.isOkFor(nonAdminMessage));
        assertFalse(adminReply.isOkFor(commandMessage));
        assertFalse(adminReply.isOkFor(groupMessage));
    }

    @Test
    void testAdminPermissionCheck() {
        SportPollBot bot = createBot();

        // Admin user should have permissions
        assertTrue(bot.isAdmin(123456789L));

        // Non-admin user should not have permissions
        assertFalse(bot.isAdmin(987654321L));
    }
}
