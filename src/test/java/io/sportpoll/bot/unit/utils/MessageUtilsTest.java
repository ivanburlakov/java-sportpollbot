package io.sportpoll.bot.unit.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import io.sportpoll.bot.config.Config;
import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.TelegramClientService;
import io.sportpoll.bot.utils.MessageUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MessageUtilsTest {

    @Mock
    private TelegramClient telegramClient;
    @Mock
    private PollManager pollManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Config.setInstance(TestUtils.createTestConfig());
    }

    @Test
    void testRouteUpdateWithPollAnswer() throws TelegramApiException {
        // Create update with poll answer
        Update update = mock(Update.class);
        when(update.hasPollAnswer()).thenReturn(true);
        // Route update to poll manager
        MessageUtils.routeUpdate(update, pollManager);
        // Verify direct vote handling was called
        verify(pollManager, times(1)).handleDirectVote(update);
    }

    @Test
    void testRouteUpdateWithCallbackQuery() throws TelegramApiException {
        // Setup update with callback query
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        User user = mock(User.class);
        Message message = mock(Message.class);
        // Configure callback query components
        when(update.hasPollAnswer()).thenReturn(false);
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getFrom()).thenReturn(user);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(callbackQuery.getData()).thenReturn("test_callback_data");
        when(user.getId()).thenReturn(123456789L);
        when(message.getChatId()).thenReturn(987654321L);
        // Route callback query update
        MessageUtils.routeUpdate(update, pollManager);
        // Verify callback query was processed
        verify(callbackQuery, atLeastOnce()).getFrom();
    }

    @Test
    void testSendMessage() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            // Setup Telegram client service mock
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class)))
                .thenReturn(mock(Message.class));
            // Send message and verify no exceptions
            assertDoesNotThrow(() -> MessageUtils.sendMessage("Test message", 123456789L));

            verify(telegramClient, times(1))
                .execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class));
        }
    }

    @Test
    void testSendError() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class)))
                .thenReturn(mock(Message.class));

            assertDoesNotThrow(() -> MessageUtils.sendError("Error message", 123456789L));

            verify(telegramClient, times(1))
                .execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class));
        }
    }

    @Test
    void testAcknowledgeCallback() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            when(telegramClient.execute(any(org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery.class)))
                .thenReturn(true);

            Update update = mock(Update.class);
            CallbackQuery callbackQuery = mock(CallbackQuery.class);
            when(update.hasCallbackQuery()).thenReturn(true);
            when(update.getCallbackQuery()).thenReturn(callbackQuery);
            when(callbackQuery.getId()).thenReturn("callback123");

            assertDoesNotThrow(() -> MessageUtils.acknowledgeCallback(update, "Acknowledged"));

            verify(telegramClient, times(1))
                .execute(any(org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery.class));
        }
    }

    @Test
    void testRouteUpdateWithNullUpdate() throws TelegramApiException {
        assertDoesNotThrow(() -> MessageUtils.routeUpdate(null, pollManager));
        verifyNoInteractions(pollManager);
    }

    @Test
    void testRouteUpdateWithDirectVoteAnswer() throws TelegramApiException {
        Update directVoteUpdate = TestUtils.createMockDirectVoteUpdate(123456789L, "TestUser", 0);

        MessageUtils.routeUpdate(directVoteUpdate, pollManager);

        verify(pollManager, times(1)).handleDirectVote(directVoteUpdate);
    }
}
