package io.sportpoll.bot.unit;

import java.util.concurrent.ScheduledExecutorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import io.sportpoll.bot.SportPollBot;
import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.WeeklyPollScheduler;
import io.sportpoll.bot.unit.utils.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    }

    @Test
    void testCreatorId() {
        SportPollBot bot = createBot();
        assertEquals(123456789L, bot.creatorId());
    }

    @Test
    void testIsAdmin() {
        SportPollBot bot = createBot();
        assertTrue(bot.isAdmin(123456789L));
        assertFalse(bot.isAdmin(987654321L));
    }

    @Test
    void testVoteReplyFiltering() {
        SportPollBot bot = createBot();
        Reply voteReply = bot.voteReply();
        Update validVote = TestUtils.createMockUpdate("/+1", -1001234567890L, 123456789L);
        Update wrongChat = TestUtils.createMockUpdate("/+1", 999999999L, 123456789L);
        Update nonVoteCommand = TestUtils.createMockUpdate("/help", -1001234567890L, 123456789L);
        assertTrue(voteReply.isOkFor(validVote));
        assertFalse(voteReply.isOkFor(wrongChat));
        assertFalse(voteReply.isOkFor(nonVoteCommand));
    }

    @Test
    void testRevokeReplyFiltering() {
        SportPollBot bot = createBot();
        Reply revokeReply = bot.revokeReply();
        Update validRevoke = TestUtils.createMockUpdate("/-1", -1001234567890L, 123456789L);
        Update wrongChat = TestUtils.createMockUpdate("/-1", 999999999L, 123456789L);
        Update voteCommand = TestUtils.createMockUpdate("/+1", -1001234567890L, 123456789L);
        assertTrue(revokeReply.isOkFor(validRevoke));
        assertFalse(revokeReply.isOkFor(wrongChat));
        assertFalse(revokeReply.isOkFor(voteCommand));
    }

    @Test
    void testAdminMessageReplyFiltering() {
        SportPollBot bot = createBot();
        Reply adminReply = bot.adminMessageReply();
        Update adminMessage = TestUtils.createMockUpdate("admin text", 123456789L, 123456789L);
        Update nonAdminMessage = TestUtils.createMockUpdate("text", 987654321L, 987654321L);
        Update commandMessage = TestUtils.createMockUpdate("/command", 123456789L, 123456789L);
        Update groupMessage = TestUtils.createMockUpdate("text", -1001234567890L, 123456789L);
        assertTrue(adminReply.isOkFor(adminMessage));
        assertFalse(adminReply.isOkFor(nonAdminMessage));
        assertFalse(adminReply.isOkFor(commandMessage));
        assertFalse(adminReply.isOkFor(groupMessage));
    }
}
