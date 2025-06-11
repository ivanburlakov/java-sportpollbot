package io.sportpoll.bot.functional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import io.sportpoll.bot.SportPollBot;
import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.TelegramClientService;
import io.sportpoll.bot.services.WeeklyPollScheduler;
import io.sportpoll.bot.unit.utils.TestUtils;

import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PollRevocationTest {

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
        TestUtils.setupTestDatabaseDirectory("poll-revocation");
        io.sportpoll.bot.config.Config.setInstance(TestUtils.createTestConfig());
    }

    @AfterEach
    void tearDown() {
        TestUtils.cleanupDatabaseFiles(originalUserDir, "PollRevocationTest_");
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void testCompleteRevocationWorkflow() throws Exception {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup real poll manager for revocation testing
            PollManager realPollManager = new PollManager(-1001234567890L);
            SportPollBot bot = TestUtils.createTestBot(telegramClient,
                realPollManager,
                weeklyPollScheduler,
                scheduler,
                "PollRevocationTest_" + testCounter + "_" + System.currentTimeMillis());

            // Mock telegram API responses
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            Message mockMessage = mock(Message.class);
            when(mockMessage.getMessageId()).thenReturn(123);
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class)))
                .thenReturn(mockMessage);
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.polls.SendPoll.class)))
                .thenReturn(mockMessage);
            when(telegramClient
                .execute(any(org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage.class)))
                    .thenReturn(true);

            // Create poll and add votes for revocation testing
            Update setupUpdate = TestUtils.createMockUpdate("setup", -1001234567890L, 123456789L);
            realPollManager.createAndPostPoll("Revocation Test", "Yes", "No", 10, setupUpdate);

            // Add multiple votes
            Update vote1 = TestUtils.createMockUpdate("/+1", -1001234567890L, 111111111L);
            Update vote2 = TestUtils.createMockUpdate("/+1", -1001234567890L, 222222222L);
            bot.voteReply().action().accept(bot, vote1);
            bot.voteReply().action().accept(bot, vote2);
            assertEquals(2, realPollManager.getPositiveVotes());

            // Test basic revocation
            Update basicRevokeUpdate = TestUtils.createMockUpdate("/-", -1001234567890L, 111111111L);
            bot.revokeReply().action().accept(bot, basicRevokeUpdate);
            assertEquals(1, realPollManager.getPositiveVotes());

            // Test numbered revocation - user 222222222L revokes their own vote (now vote
            // #1 after first revocation)
            Update numberedRevokeUpdate = TestUtils.createMockUpdate("/- 1", -1001234567890L, 222222222L);
            bot.revokeReply().action().accept(bot, numberedRevokeUpdate);
            assertEquals(0, realPollManager.getPositiveVotes());
        }
    }

    @Test
    void testBothRevocationFormatsWork() throws Exception {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            PollManager realPollManager = new PollManager(-1001234567890L);
            SportPollBot bot = TestUtils.createTestBot(telegramClient,
                realPollManager,
                weeklyPollScheduler,
                scheduler,
                "PollRevocationTest_Formats_" + testCounter + "_" + System.currentTimeMillis());

            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            Message mockMessage = mock(Message.class);
            when(mockMessage.getMessageId()).thenReturn(123);
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class)))
                .thenReturn(mockMessage);
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.polls.SendPoll.class)))
                .thenReturn(mockMessage);
            when(telegramClient
                .execute(any(org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage.class)))
                    .thenReturn(true);

            Update setupUpdate = TestUtils.createMockUpdate("setup", -1001234567890L, 123456789L);
            realPollManager.createAndPostPoll("Revocation Format Test", "Positive", "Negative", 10, setupUpdate);

            Update vote1 = TestUtils.createMockUpdate("/+ Alice", -1001234567890L, 123456789L);
            bot.voteReply().action().accept(bot, vote1);
            Update vote2 = TestUtils.createMockUpdate("/+ Bob", -1001234567890L, 456456456L);
            bot.voteReply().action().accept(bot, vote2);
            Update vote3 = TestUtils.createMockUpdate("/+ Charlie", -1001234567890L, 789789789L);
            bot.voteReply().action().accept(bot, vote3);
            assertEquals(3, realPollManager.getPositiveVotes());

            Update spaceRevoke = TestUtils.createMockUpdate("/- 2", -1001234567890L, 456456456L);
            bot.revokeReply().action().accept(bot, spaceRevoke);
            assertEquals(2, realPollManager.getPositiveVotes());

            Update noSpaceRevoke = TestUtils.createMockUpdate("/-3", -1001234567890L, 789789789L);
            bot.revokeReply().action().accept(bot, noSpaceRevoke);
            assertEquals(1, realPollManager.getPositiveVotes());
        }
    }
}
