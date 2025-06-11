package io.sportpoll.bot.unit.utils;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import io.sportpoll.bot.utils.ExceptionHandler;
import static org.junit.jupiter.api.Assertions.*;

public class ExceptionHandlerTest {

    @Test
    void testHandleSuccessfulAction() {
        // Test exception handler with successful action
        assertDoesNotThrow(() -> ExceptionHandler.handle(() -> {
            // Action that succeeds
        }));
    }

    @Test
    void testHandleTelegramApiException() {
        // Test exception handler wraps TelegramApiException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> ExceptionHandler.handle(() -> {
            throw new TelegramApiException("Test exception");
        }));
        // Verify exception is properly wrapped
        assertTrue(exception.getCause() instanceof TelegramApiException);
        assertEquals("Test exception", exception.getCause().getMessage());
    }
}
