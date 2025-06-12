package io.sportpoll.bot.unit.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import io.sportpoll.bot.config.Config;
import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.TelegramClientService;
import io.sportpoll.bot.utils.MessageUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
    void testSendMessage() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            when(telegramClient.execute(any(SendMessage.class))).thenReturn(mock(Message.class));
            assertDoesNotThrow(() -> MessageUtils.sendMessage("Test message", 123456789L));
            verify(telegramClient, times(1)).execute(any(SendMessage.class));
        }
    }

    @Test
    void testSendError() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            when(telegramClient.execute(any(SendMessage.class))).thenReturn(mock(Message.class));
            assertDoesNotThrow(() -> MessageUtils.sendError("Error message", 123456789L));
            verify(telegramClient, times(1)).execute(any(SendMessage.class));
        }
    }

    @Test
    void testAcknowledgeCallback() throws TelegramApiException {
        try (MockedStatic<TelegramClientService> mockedStatic = mockStatic(TelegramClientService.class)) {
            mockedStatic.when(TelegramClientService::getInstance).thenReturn(telegramClient);
            when(telegramClient.execute(any(AnswerCallbackQuery.class))).thenReturn(true);
            Update update = mock(Update.class);
            CallbackQuery callbackQuery = mock(CallbackQuery.class);
            when(update.hasCallbackQuery()).thenReturn(true);
            when(update.getCallbackQuery()).thenReturn(callbackQuery);
            when(callbackQuery.getId()).thenReturn("callback123");
            assertDoesNotThrow(() -> MessageUtils.acknowledgeCallback(update, "Acknowledged"));
            verify(telegramClient, times(1)).execute(any(AnswerCallbackQuery.class));
        }
    }

    @Test
    void testRouteUpdateWithNullUpdate() throws TelegramApiException {
        assertDoesNotThrow(() -> MessageUtils.routeUpdate(null, pollManager));
        verifyNoInteractions(pollManager);
    }

    @Test
    void testRouteUpdateWithPollAnswer() throws TelegramApiException {
        Update update = mock(Update.class);
        when(update.hasPollAnswer()).thenReturn(true);
        MessageUtils.routeUpdate(update, pollManager);
        verify(pollManager, times(1)).handleDirectVote(update);
    }
}
