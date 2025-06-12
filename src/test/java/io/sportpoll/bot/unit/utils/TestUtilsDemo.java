package io.sportpoll.bot.unit.utils;

import io.sportpoll.bot.config.Config;
import io.sportpoll.bot.services.AdminSession;
import io.sportpoll.bot.services.PollManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import static org.mockito.Mockito.*;

class TestUtilsDemo {
    private PollManager pollManager;

    @BeforeEach
    void setUp() {
        Config.setInstance(TestUtils.createTestConfig());
        pollManager = new PollManager(-1001234567890L);
    }

    @Test
    void demonstratePollTestBuilder() throws Exception {
        TestUtils.executeWithTelegramMock(setup -> {
            TestUtils.pollBuilder()
                .withQuestion("Will you attend the meeting?")
                .withOptions("Yes, I'll be there", "No, can't make it")
                .withVoteLimit(10)
                .createPoll(pollManager);

            TestUtils.assertPollState(pollManager, true, 0);

            Update voteUpdate = TestUtils.createMockUpdate("test", -1001234567890L, 123456789L);
            pollManager.addExternalVote(new String[] { "Alice", "Bob" }, 2, voteUpdate);

            TestUtils.assertPollState(pollManager, true, 2);
        });
    }

    @Test
    void demonstrateDirectVoteHandling() throws Exception {
        TestUtils.executeWithTelegramMock(setup -> {
            TestUtils.pollBuilder().createPoll(pollManager);

            Update positiveVote = TestUtils.createMockDirectVoteUpdate(123L, "Alice", 0);
            pollManager.handleDirectVote(positiveVote);
            TestUtils.assertPollState(pollManager, true, 1);

            Update negativeVote = TestUtils.createMockDirectVoteUpdate(456L, "Bob", 1);
            pollManager.handleDirectVote(negativeVote);
            TestUtils.assertPollState(pollManager, true, 1);

            Update voteRemoval = TestUtils.createMockDirectVoteUpdate(123L, "Alice");
            pollManager.handleDirectVote(voteRemoval);
            TestUtils.assertPollState(pollManager, true, 1);
        });
    }

    @Test
    void demonstrateVerificationHelpers() throws Exception {
        TestUtils.executeWithTelegramMock(setup -> {
            TelegramClient client = setup.getTelegramClient();

            TestUtils.pollBuilder().createPoll(pollManager);

            TestUtils.verifyPollCreated(client);
            verify(client, atLeast(1)).execute(any(org.telegram.telegrambots.meta.api.methods.send.SendMessage.class));
            TestUtils.verifyPollPinned(client);
        });
    }

    @Test
    void demonstrateAdminSessionMocking() throws Exception {
        TestUtils.executeWithDataStoreMock(setup -> {
            TelegramClient telegramClient = mock(TelegramClient.class);
            Update update = TestUtils.createMockUpdate("/start", 12345L, 67890L);

            AdminSession adminSession = new AdminSession(update, telegramClient, pollManager);

            TestUtils.assertNoErrorsThrowable(() -> adminSession.handleUpdate(update));
        });
    }

    @Test
    void demonstrateCallbackHandling() throws Exception {
        TestUtils.executeWithDataStoreMock(setup -> {
            TelegramClient telegramClient = mock(TelegramClient.class);
            Update callbackUpdate = TestUtils.createMockCallbackUpdate("main:menu", 12345L, 67890L);

            AdminSession adminSession = new AdminSession(callbackUpdate, telegramClient, pollManager);

            TestUtils.assertNoErrorsThrowable(() -> adminSession.handleUpdate(callbackUpdate));
        });
    }
}
