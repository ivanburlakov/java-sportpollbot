package io.sportpoll.bot.unit.services;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import io.sportpoll.bot.config.Config;
import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.TelegramClientService;
import io.sportpoll.bot.unit.utils.TestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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
    void testPollCreationAndVoting() throws Exception {
        TestUtils.executeWithTelegramMock(setup -> {
            TestUtils.pollBuilder().withVoteLimit(5).createPoll(pollManager);

            TestUtils.assertPollState(pollManager, true, 0);

            Update update = TestUtils.createMockUpdate("test", -1001234567890L, 123456789L);
            pollManager.addExternalVote(null, 1, update);

            TestUtils.assertPollState(pollManager, true, 1);
        });
    }

    @Test
    void testVoteLimitEnforcement() throws Exception {
        TestUtils.executeWithTelegramMock(setup -> {
            TestUtils.pollBuilder().withVoteLimit(3).createPoll(pollManager);

            Update update = TestUtils.createMockUpdate("test", -1001234567890L, 123456789L);
            pollManager.addExternalVote(null, 4, update);

            TestUtils.assertPollState(pollManager, true, 0);
        });
    }

    @Test
    void testNamedVotes() throws Exception {
        TestUtils.executeWithTelegramMock(setup -> {
            TestUtils.pollBuilder().withVoteLimit(5).createPoll(pollManager);

            Update update = TestUtils.createMockUpdate("test", -1001234567890L, 123456789L);
            pollManager.addExternalVote(new String[] { "John", "Jane" }, 2, update);

            TestUtils.assertPollState(pollManager, true, 2);
        });
    }

    @Test
    void testHandleVoteCommand() throws Exception {
        TestUtils.executeWithTelegramMock(setup -> {
            TestUtils.pollBuilder().withVoteLimit(5).createPoll(pollManager);

            Update update = TestUtils.createMockUpdate("/+", -1001234567890L, 123456789L);
            pollManager.handleVoteCommand(update);

            TestUtils.assertPollState(pollManager, true, 1);
        });
    }

    @Test
    void testHandleRevokeCommand() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup mock Telegram services
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            Message mockMessage = mock(Message.class);
            when(mockMessage.getMessageId()).thenReturn(123);
            when(telegramClient.execute(any(SendMessage.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(SendPoll.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(PinChatMessage.class))).thenReturn(true);

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
    void testVoteCommandWithNoActivePoll() throws Exception {
        TestUtils.executeWithTelegramMock(setup -> {
            Update update = TestUtils.createMockUpdate("/+", -1001234567890L, 123456789L);

            TestUtils.assertNoErrorsThrowable(() -> pollManager.handleVoteCommand(update));
        });
    }

    @Test
    void testResetPoll() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup mocks including unpin functionality
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            Message mockMessage = mock(Message.class);
            when(mockMessage.getMessageId()).thenReturn(123);
            when(telegramClient.execute(any(SendMessage.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(SendPoll.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(PinChatMessage.class))).thenReturn(true);
            when(telegramClient.execute(any(UnpinChatMessage.class))).thenReturn(true);

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
            when(telegramClient.execute(any(SendMessage.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(SendPoll.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(PinChatMessage.class))).thenReturn(true);

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
            when(telegramClient.execute(any(SendMessage.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(SendPoll.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(PinChatMessage.class))).thenReturn(true);

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
            when(telegramClient.execute(any(SendMessage.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(SendPoll.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(PinChatMessage.class))).thenReturn(true);

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
            when(telegramClient.execute(any(SendMessage.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(SendPoll.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(PinChatMessage.class))).thenReturn(true);

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
            when(telegramClient.execute(any(SendMessage.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(SendPoll.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(PinChatMessage.class))).thenReturn(true);

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
            when(telegramClient.execute(any(SendMessage.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(SendPoll.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(PinChatMessage.class))).thenReturn(true);

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
            when(telegramClient.execute(any(SendMessage.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(SendPoll.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(PinChatMessage.class))).thenReturn(true);

            // Create poll and vote with numeric argument
            Update update = TestUtils.createMockUpdate("/+ 3", -1001234567890L, 123456789L);
            pollManager.createAndPostPoll("Test question", "Yes", "No", 5, update);
            // Process vote command with number 3
            pollManager.handleVoteCommand(update);
            // Verify 3 anonymous votes added
            assertEquals(3, pollManager.getPositiveVotes());
        }
    }

    @Test
    void testHandleNegativeDirectVote() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            Message mockMessage = mock(Message.class);
            when(mockMessage.getMessageId()).thenReturn(123);
            when(telegramClient.execute(any(SendMessage.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(SendPoll.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(PinChatMessage.class))).thenReturn(true);

            pollManager.initializePoll(5, 123);
            Update positiveVoteUpdate = TestUtils.createMockDirectVoteUpdate(123456789L, "PositiveUser", 0);
            pollManager.handleDirectVote(positiveVoteUpdate);
            assertEquals(1, pollManager.getPositiveVotes());
            Update negativeVoteUpdate = TestUtils.createMockDirectVoteUpdate(987654321L, "NegativeUser", 1);
            pollManager.handleDirectVote(negativeVoteUpdate);
            assertEquals(1, pollManager.getPositiveVotes());
            Update anotherNegativeVote = TestUtils.createMockDirectVoteUpdate(555666777L, "AnotherNegUser", 1);
            pollManager.handleDirectVote(anotherNegativeVote);
            assertEquals(1, pollManager.getPositiveVotes());
            Update anotherPositiveVote = TestUtils.createMockDirectVoteUpdate(888999000L, "AnotherPosUser", 0);
            pollManager.handleDirectVote(anotherPositiveVote);
            assertEquals(2, pollManager.getPositiveVotes());
        }
    }

    @Test
    void testDirectVoteSwitchingBetweenOptions() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            Message mockMessage = mock(Message.class);
            when(mockMessage.getMessageId()).thenReturn(123);
            when(telegramClient.execute(any(SendMessage.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(SendPoll.class))).thenReturn(mockMessage);
            when(telegramClient.execute(any(PinChatMessage.class))).thenReturn(true);

            pollManager.initializePoll(5, 123);

            User mockUser = mock(User.class);
            when(mockUser.getId()).thenReturn(123456789L);
            when(mockUser.getFirstName()).thenReturn("TestUser");

            Update initialPositiveVote = mock(Update.class);
            PollAnswer pollAnswer1 = mock(PollAnswer.class);
            when(initialPositiveVote.hasPollAnswer()).thenReturn(true);
            when(initialPositiveVote.getPollAnswer()).thenReturn(pollAnswer1);
            when(pollAnswer1.getUser()).thenReturn(mockUser);
            when(pollAnswer1.getOptionIds()).thenReturn(List.of(0));

            pollManager.handleDirectVote(initialPositiveVote);
            assertEquals(1, pollManager.getPositiveVotes());

            Update switchToNegative = mock(Update.class);
            PollAnswer pollAnswer2 = mock(PollAnswer.class);
            when(switchToNegative.hasPollAnswer()).thenReturn(true);
            when(switchToNegative.getPollAnswer()).thenReturn(pollAnswer2);
            when(pollAnswer2.getUser()).thenReturn(mockUser);
            when(pollAnswer2.getOptionIds()).thenReturn(List.of(1));

            pollManager.handleDirectVote(switchToNegative);
            assertEquals(0, pollManager.getPositiveVotes());

            Update switchBackToPositive = mock(Update.class);
            PollAnswer pollAnswer3 = mock(PollAnswer.class);
            when(switchBackToPositive.hasPollAnswer()).thenReturn(true);
            when(switchBackToPositive.getPollAnswer()).thenReturn(pollAnswer3);
            when(pollAnswer3.getUser()).thenReturn(mockUser);
            when(pollAnswer3.getOptionIds()).thenReturn(List.of(0));

            pollManager.handleDirectVote(switchBackToPositive);
            assertEquals(1, pollManager.getPositiveVotes());

            Update removeVote = mock(Update.class);
            PollAnswer pollAnswer4 = mock(PollAnswer.class);
            when(removeVote.hasPollAnswer()).thenReturn(true);
            when(removeVote.getPollAnswer()).thenReturn(pollAnswer4);
            when(pollAnswer4.getUser()).thenReturn(mockUser);
            when(pollAnswer4.getOptionIds()).thenReturn(List.of());

            pollManager.handleDirectVote(removeVote);
            assertEquals(0, pollManager.getPositiveVotes());
        }
    }
}
