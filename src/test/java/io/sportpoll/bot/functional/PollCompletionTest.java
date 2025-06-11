package io.sportpoll.bot.functional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
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

public class PollCompletionTest {

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
        TestUtils.setupTestDatabaseDirectory("poll-completion");
    }

    @AfterEach
    void tearDown() {
        TestUtils.cleanupDatabaseFiles(originalUserDir, "PollCompletionTest_");
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void testCompletePollCompletionWorkflow() throws Exception {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup real components for completion testing
            PollManager realPollManager = new PollManager(-1001234567890L);
            SportPollBot bot = TestUtils.createTestBot(telegramClient,
                realPollManager,
                weeklyPollScheduler,
                scheduler,
                "PollCompletionTest_" + testCounter + "_" + System.currentTimeMillis());

            // Mock telegram API responses including poll stopping
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
            Poll mockPoll = mock(Poll.class);
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.polls.StopPoll.class)))
                .thenReturn(mockPoll);

            // Create poll with low target for completion test
            Update setupUpdate = TestUtils.createMockUpdate("setup", -1001234567890L, 123456789L);
            realPollManager.createAndPostPoll("Completion Test", "Yes", "No", 2, setupUpdate);
            assertTrue(realPollManager.hasActivePoll());

            // Add votes to reach completion
            Update vote1 = TestUtils.createMockUpdate("/+1", -1001234567890L, 111111111L);
            Update vote2 = TestUtils.createMockUpdate("/+1", -1001234567890L, 222222222L);

            bot.voteReply().action().accept(bot, vote1);
            bot.voteReply().action().accept(bot, vote2);

            // Verify poll completion automatically closed the poll
            assertEquals(2, realPollManager.getPositiveVotes());
            assertFalse(realPollManager.hasActivePoll());
        }
    }
}
