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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import io.sportpoll.bot.SportPollBot;
import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.TelegramClientService;
import io.sportpoll.bot.services.WeeklyPollScheduler;
import io.sportpoll.bot.unit.utils.TestUtils;

import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PollVotingTest {

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
        TestUtils.setupTestDatabaseDirectory("poll-voting");
    }

    @AfterEach
    void tearDown() {
        TestUtils.cleanupDatabaseFiles(originalUserDir, "PollVotingTest_");
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void testCompleteVotingWorkflow() throws Exception {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup real poll manager for voting testing
            PollManager realPollManager = new PollManager(-1001234567890L);
            SportPollBot bot = TestUtils.createTestBot(telegramClient,
                realPollManager,
                weeklyPollScheduler,
                scheduler,
                "PollVotingTest_" + testCounter + "_" + System.currentTimeMillis());

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

            // Create poll for voting workflow
            Update setupUpdate = TestUtils.createMockUpdate("setup", -1001234567890L, 123456789L);
            realPollManager.createAndPostPoll("Voting Test Poll", "Positive", "Negative", 10, setupUpdate);

            // Test single vote
            Update singleVoteUpdate = TestUtils.createMockUpdate("/+1", -1001234567890L, 123456789L);
            bot.voteReply().action().accept(bot, singleVoteUpdate);
            assertEquals(1, realPollManager.getPositiveVotes());

            // Test multi-named vote
            Update namedVoteUpdate = TestUtils.createMockUpdate("/+ Alice Bob", -1001234567890L, 456456456L);
            bot.voteReply().action().accept(bot, namedVoteUpdate);
            assertEquals(3, realPollManager.getPositiveVotes());

            // Test numeric vote
            Update numericVoteUpdate = TestUtils.createMockUpdate("/+ 2", -1001234567890L, 789789789L);
            bot.voteReply().action().accept(bot, numericVoteUpdate);
            assertEquals(5, realPollManager.getPositiveVotes());
        }
    }

    @Test
    void testVotingErrorHandling() throws TelegramApiException {
        // Setup for error testing
        PollManager mockPollManager = mock(PollManager.class);
        SportPollBot bot = TestUtils.createTestBot(telegramClient,
            mockPollManager,
            weeklyPollScheduler,
            scheduler,
            "PollVotingTest_Error_" + testCounter + "_" + System.currentTimeMillis());

        // Mock exception on vote command
        Update voteUpdate = TestUtils.createMockUpdate("/+1", -1001234567890L, 123456789L);
        doThrow(new TelegramApiException("API Error")).when(mockPollManager).handleVoteCommand(any());

        // Verify exception is properly wrapped and handled
        assertThrows(RuntimeException.class, () -> bot.voteReply().action().accept(bot, voteUpdate));
        verify(mockPollManager, times(1)).handleVoteCommand(any());
    }

    @Test
    void testBothCommandFormatsWork() throws Exception {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            PollManager realPollManager = new PollManager(-1001234567890L);
            SportPollBot bot = TestUtils.createTestBot(telegramClient,
                realPollManager,
                weeklyPollScheduler,
                scheduler,
                "PollVotingTest_Formats_" + testCounter + "_" + System.currentTimeMillis());

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
            realPollManager.createAndPostPoll("Format Test Poll", "Positive", "Negative", 10, setupUpdate);

            Update spaceFormat1 = TestUtils.createMockUpdate("/+ 3", -1001234567890L, 123456789L);
            bot.voteReply().action().accept(bot, spaceFormat1);
            assertEquals(3, realPollManager.getPositiveVotes());

            Update noSpaceFormat1 = TestUtils.createMockUpdate("/+2", -1001234567890L, 456456456L);
            bot.voteReply().action().accept(bot, noSpaceFormat1);
            assertEquals(5, realPollManager.getPositiveVotes());

            Update spaceFormat2 = TestUtils.createMockUpdate("/+ Alice Bob", -1001234567890L, 789789789L);
            bot.voteReply().action().accept(bot, spaceFormat2);
            assertEquals(7, realPollManager.getPositiveVotes());

            Update noSpaceFormat2 = TestUtils.createMockUpdate("/+Charlie David", -1001234567890L, 987654321L);
            bot.voteReply().action().accept(bot, noSpaceFormat2);
            assertEquals(9, realPollManager.getPositiveVotes());
        }
    }
}
