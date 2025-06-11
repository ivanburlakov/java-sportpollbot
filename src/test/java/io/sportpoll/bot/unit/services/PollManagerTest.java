package io.sportpoll.bot.unit.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import io.sportpoll.bot.config.Config;
import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.TelegramClientService;
import io.sportpoll.bot.unit.utils.TestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PollManagerTest {
    @Mock
    private TelegramClient telegramClient;
    private PollManager pollManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Config.setInstance(TestUtils.createTestConfig());
        pollManager = new PollManager(-1001234567890L);
    }

    @Test
    void testPollCreationAndVoting() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Mock Telegram client service for API calls
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            Message mockMessage = mock(Message.class);
            when(mockMessage.getMessageId()).thenReturn(123);
            // Mock successful message sending and poll posting
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class)))
                .thenReturn(mockMessage);
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.polls.SendPoll.class)))
                .thenReturn(mockMessage);
            when(telegramClient
                .execute(any(org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage.class)))
                    .thenReturn(true);

            // Create poll with 5 vote limit
            Update update = TestUtils.createMockUpdate("test", -1001234567890L, 123456789L);
            pollManager.createAndPostPoll("Test question", "Yes", "No", 5, update);
            // Verify poll is active
            assertTrue(pollManager.hasActivePoll());
            // Add single external vote
            pollManager.addExternalVote(null, 1, update);
            // Verify vote was counted
            assertEquals(1, pollManager.getPositiveVotes());
        }
    }

    @Test
    void testVoteLimitEnforcement() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup Telegram service mocks
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

            // Create poll with 3-vote limit
            Update update = TestUtils.createMockUpdate("test", -1001234567890L, 123456789L);
            pollManager.createAndPostPoll("Test question", "Yes", "No", 3, update);
            // Try adding 4 votes (exceeds limit)
            pollManager.addExternalVote(null, 4, update);
            // Verify all votes rejected due to limit breach
            assertEquals(0, pollManager.getPositiveVotes());
        }
    }

    @Test
    void testNamedVotes() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup mock Telegram responses
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

            // Create poll and add named votes
            Update update = TestUtils.createMockUpdate("test", -1001234567890L, 123456789L);
            pollManager.createAndPostPoll("Test question", "Yes", "No", 5, update);
            // Add 2 named votes for John and Jane
            pollManager.addExternalVote(new String[] { "John", "Jane" }, 2, update);
            // Verify vote count matches names provided
            assertEquals(2, pollManager.getPositiveVotes());
        }
    }

    @Test
    void testHandleVoteCommand() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup mock services
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

            // Create poll and handle basic /+ command
            Update update = TestUtils.createMockUpdate("/+", -1001234567890L, 123456789L);
            pollManager.createAndPostPoll("Test question", "Yes", "No", 5, update);
            // Process single positive vote command
            pollManager.handleVoteCommand(update);
            // Verify vote registered
            assertEquals(1, pollManager.getPositiveVotes());
        }
    }

    @Test
    void testHandleRevokeCommand() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup mock Telegram services
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

            // Create poll and add vote
            Update update = TestUtils.createMockUpdate("/-", -1001234567890L, 123456789L);
            pollManager.createAndPostPoll("Test question", "Yes", "No", 5, update);
            pollManager.addExternalVote(null, 1, update);
            // Verify vote added
            assertEquals(1, pollManager.getPositiveVotes());
            // Handle revoke command to remove vote
            pollManager.handleRevokeCommand(update);
            // Verify vote removed
            assertEquals(0, pollManager.getPositiveVotes());
        }
    }

    @Test
    void testVoteCommandWithNoActivePoll() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup minimal mocks for no-poll scenario
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class)))
                .thenReturn(mock(Message.class));

            // Try voting when no poll exists
            Update update = TestUtils.createMockUpdate("/+", -1001234567890L, 123456789L);
            // Verify operation completes without errors
            assertDoesNotThrow(() -> pollManager.handleVoteCommand(update));
        }
    }

    @Test
    void testResetPoll() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup mocks including unpin functionality
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
            when(telegramClient
                .execute(any(org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage.class)))
                    .thenReturn(true);

            // Create active poll
            Update update = TestUtils.createMockUpdate("test", -1001234567890L, 123456789L);
            pollManager.createAndPostPoll("Test question", "Yes", "No", 5, update);
            // Verify poll is active
            assertTrue(pollManager.hasActivePoll());
            // Reset poll completely
            pollManager.resetPollPublic();
            // Verify poll is no longer active
            assertFalse(pollManager.hasActivePoll());
        }
    }

    @Test
    void testRevokeByVoteNumber() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup mock Telegram service responses
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

            // Create poll and add multiple votes
            Update update = TestUtils.createMockUpdate("/-", -1001234567890L, 123456789L);
            pollManager.createAndPostPoll("Test question", "Yes", "No", 5, update);
            pollManager.addExternalVote(new String[] { "John", "Jane" }, 2, update);
            pollManager.addExternalVote(null, 1, update);
            // Verify all votes counted
            assertEquals(3, pollManager.getPositiveVotes());
            // Revoke vote #2 specifically
            Update revokeUpdate = TestUtils.createMockUpdate("/- 2", -1001234567890L, 123456789L);
            pollManager.handleRevokeCommand(revokeUpdate);
            // Verify vote removed
            assertEquals(2, pollManager.getPositiveVotes());
        }
    }

    @Test
    void testRevokeInvalidVoteNumber() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup standard mocks
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

            // Create poll with single vote
            Update update = TestUtils.createMockUpdate("test", -1001234567890L, 123456789L);
            pollManager.createAndPostPoll("Test question", "Yes", "No", 5, update);
            pollManager.addExternalVote(null, 1, update);
            // Try revoking non-existent vote #99
            Update revokeUpdate = TestUtils.createMockUpdate("/- 99", -1001234567890L, 123456789L);
            // Verify operation completes without errors
            assertDoesNotThrow(() -> pollManager.handleRevokeCommand(revokeUpdate));
            // Verify original vote remains untouched
            assertEquals(1, pollManager.getPositiveVotes());
        }
    }

    @Test
    void testHandleDirectVote() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup mocks for direct vote handling
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

            // Initialize poll for direct voting
            pollManager.initializePoll(5, 123);
            // Process first direct vote for option 0 (positive)
            Update directVoteUpdate = TestUtils.createMockDirectVoteUpdate(123456789L, "TestUser", 0);
            pollManager.handleDirectVote(directVoteUpdate);
            // Verify first vote counted
            assertEquals(1, pollManager.getPositiveVotes());
            // Process second direct vote from different user
            Update directVoteUpdate2 = TestUtils.createMockDirectVoteUpdate(987654321L, "TestUser2", 0);
            pollManager.handleDirectVote(directVoteUpdate2);
            // Verify both votes counted
            assertEquals(2, pollManager.getPositiveVotes());
        }
    }

    @Test
    void testDirectVoteRevoke() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup mocks for direct vote revoke scenario
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

            // Initialize poll for direct voting
            pollManager.initializePoll(5, 123);
            // Add direct vote from user
            Update directVoteUpdate = TestUtils.createMockDirectVoteUpdate(123456789L, "TestUser", 0);
            pollManager.handleDirectVote(directVoteUpdate);
            // Verify vote registered
            assertEquals(1, pollManager.getPositiveVotes());
            // Send empty vote update (revoke vote)
            Update emptyVoteUpdate = TestUtils.createMockDirectVoteUpdate(123456789L, "TestUser");
            pollManager.handleDirectVote(emptyVoteUpdate);
            // Verify vote was revoked
            assertEquals(0, pollManager.getPositiveVotes());
        }
    }

    @Test
    void testAdminRevokePermissions() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
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

            // Create poll as non-admin user
            Update nonAdminUpdate = TestUtils.createMockUpdate("test", -1001234567890L, 999999999L);
            pollManager.createAndPostPoll("Test question", "Yes", "No", 5, nonAdminUpdate);
            // Add vote from non-admin
            pollManager.addExternalVote(null, 1, nonAdminUpdate);
            // Verify vote added
            assertEquals(1, pollManager.getPositiveVotes());
            // Admin revokes specific vote by number
            Update adminRevokeUpdate = TestUtils.createMockUpdate("/- 1", -1001234567890L, 123456789L);
            pollManager.handleRevokeCommand(adminRevokeUpdate);
            // Verify admin can revoke any vote
            assertEquals(0, pollManager.getPositiveVotes());
        }
    }

    @Test
    void testVoteCommandWithMultipleArguments() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup standard mocks
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

            // Create poll and vote with multiple named arguments
            Update update = TestUtils.createMockUpdate("/+ John Jane Bob", -1001234567890L, 123456789L);
            pollManager.createAndPostPoll("Test question", "Yes", "No", 5, update);
            // Process vote command with 3 names
            pollManager.handleVoteCommand(update);
            // Verify all 3 votes counted
            assertEquals(3, pollManager.getPositiveVotes());
        }
    }

    @Test
    void testVoteCommandWithNumericArgument() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup standard mock responses
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

            // Create poll and vote with numeric argument
            Update update = TestUtils.createMockUpdate("/+ 3", -1001234567890L, 123456789L);
            pollManager.createAndPostPoll("Test question", "Yes", "No", 5, update);
            // Process vote command with number 3
            pollManager.handleVoteCommand(update);
            // Verify 3 anonymous votes added
            assertEquals(3, pollManager.getPositiveVotes());
        }
    }
}
