package io.sportpoll.bot.unit.commands;

import io.sportpoll.bot.commands.PollCommand;
import io.sportpoll.bot.commands.PollCommand.PollCreationResult;
import io.sportpoll.bot.config.Config;
import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.TelegramClientService;
import io.sportpoll.bot.unit.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PollCommandTest {
    private PollManager pollManager;
    private TelegramClient telegramClient;
    private PollCommand pollCommand;

    @BeforeEach
    void setUp() {
        Config.setInstance(TestUtils.createTestConfig());
        pollManager = mock(PollManager.class);
        telegramClient = mock(TelegramClient.class);
        pollCommand = new PollCommand(pollManager);
    }

    @Test
    void testCreatePollSuccess() throws TelegramApiException {
        // Test successful poll creation with valid parameters
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Mock Telegram client service
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            // Configure poll manager to indicate no active poll
            when(pollManager.hasActivePoll()).thenReturn(false);
            // Mock successful Telegram API response
            Message message = mock(Message.class);
            when(message.getMessageId()).thenReturn(123);
            when(telegramClient.execute(any(SendPoll.class))).thenReturn(message);
            // Execute poll creation
            PollCreationResult result = pollCommand.createPoll("Test question?", "Yes", "No", 5);
            // Verify successful result
            assertTrue(result.success());
            assertEquals("Poll created successfully", result.message());
            assertEquals(123, result.messageId());
            // Verify poll manager initialization was called
            verify(pollManager).initializePoll(5, 123);
        }
    }

    @Test
    void testCreatePollWithActivePoll() {
        // Test poll creation rejection when another poll is already active
        // Configure poll manager to indicate active poll exists
        when(pollManager.hasActivePoll()).thenReturn(true);
        // Attempt to create new poll
        PollCreationResult result = pollCommand.createPoll("Test question?", "Yes", "No", 5);
        // Verify rejection due to active poll
        assertFalse(result.success());
        assertEquals("Another poll is already active", result.message());
        assertEquals(0, result.messageId());
    }

    @Test
    void testCreatePollTelegramException() throws TelegramApiException {
        // Test error handling when Telegram API throws exception
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup mocks for exception scenario
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            when(pollManager.hasActivePoll()).thenReturn(false);
            // Configure Telegram client to throw exception
            when(telegramClient.execute(any(SendPoll.class))).thenThrow(new TelegramApiException("API Error"));
            // Attempt poll creation with API error
            PollCreationResult result = pollCommand.createPoll("Test?", "Yes", "No", 5);
            // Verify error handling
            assertFalse(result.success());
            assertEquals("API Error", result.message());
            assertEquals(0, result.messageId());
            // Verify poll manager was not initialized
            verify(pollManager, never()).initializePoll(anyInt(), anyInt());
        }
    }

    @Test
    void testCreatePollNullResponse() throws TelegramApiException {
        // Test handling of null response from Telegram API
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup mocks for null response scenario
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            when(pollManager.hasActivePoll()).thenReturn(false);
            // Configure Telegram client to return null
            when(telegramClient.execute(any(SendPoll.class))).thenReturn(null);
            // Attempt poll creation with null response
            PollCreationResult result = pollCommand.createPoll("Test?", "Yes", "No", 5);
            // Verify null response handling
            assertFalse(result.success());
            assertEquals("Invalid response from Telegram API", result.message());
            assertEquals(0, result.messageId());
            // Verify poll manager was not initialized
            verify(pollManager, never()).initializePoll(anyInt(), anyInt());
        }
    }

    @Test
    void testCreatePollZeroMessageId() throws TelegramApiException {
        // Test handling of invalid message ID (zero) from Telegram API
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup mocks for zero message ID scenario
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            when(pollManager.hasActivePoll()).thenReturn(false);
            // Configure message with invalid ID
            Message message = mock(Message.class);
            when(message.getMessageId()).thenReturn(0);
            when(telegramClient.execute(any(SendPoll.class))).thenReturn(message);
            // Attempt poll creation with zero message ID
            PollCreationResult result = pollCommand.createPoll("Test?", "Yes", "No", 5);
            // Verify zero ID handling
            assertFalse(result.success());
            assertEquals("Invalid response from Telegram API", result.message());
            assertEquals(0, result.messageId());
            // Verify poll manager was not initialized
            verify(pollManager, never()).initializePoll(anyInt(), anyInt());
        }
    }
}
