package io.sportpoll.bot.commands;

import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.TelegramClientService;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.polls.input.InputPollOption;
import io.sportpoll.bot.config.Config;
import java.util.Arrays;

public class PollCommand {
    private final PollManager pollManager;

    public PollCommand(PollManager pollManager) {
        this.pollManager = pollManager;
    }

    public record PollCreationResult(boolean success, String message, int messageId) {
        public static PollCreationResult error(String message) {
            return new PollCreationResult(false, message, 0);
        }

        public static PollCreationResult success(String message, int messageId) {
            return new PollCreationResult(true, message, messageId);
        }
    }

    public PollCreationResult createPoll(String question, String positiveOption, String negativeOption,
        int targetVotes) {
        try {
            if (!validateInput(question, positiveOption, negativeOption)) {
                return PollCreationResult.error("Invalid poll parameters");
            }

            if (pollManager.hasActivePoll()) {
                return PollCreationResult.error("Another poll is already active");
            }

            var result = sendPoll(question, positiveOption, negativeOption);
            if (result.success()) {
                pollManager.initializePoll(targetVotes, result.messageId());
                return PollCreationResult.success("Poll created successfully", result.messageId());
            } else {
                return PollCreationResult.error(result.message());
            }
        } catch (Exception e) {
            return PollCreationResult.error("Failed to create poll: " + e.getMessage());
        }
    }

    private record SendPollResult(boolean success, String message, int messageId) {
    }

    private SendPollResult sendPoll(String question, String positiveOption, String negativeOption) {
        try {
            long targetGroupId = Config.getInstance().targetGroupChatId;

            var poll = SendPoll.builder()
                .chatId(String.valueOf(targetGroupId))
                .question(question)
                .options(Arrays.asList(InputPollOption.builder().text(positiveOption).build(),
                    InputPollOption.builder().text(negativeOption).build()))
                .isAnonymous(false)
                .type("regular")
                .build();

            var response = TelegramClientService.getInstance().execute(poll);
            if (response != null && response.getMessageId() != 0) {
                return new SendPollResult(true, "Poll sent", response.getMessageId());
            } else {
                return new SendPollResult(false, "Invalid response from Telegram API", 0);
            }
        } catch (Exception e) {
            return new SendPollResult(false, e.getMessage(), 0);
        }
    }

    private boolean validateInput(String question, String positiveOption, String negativeOption) {
        return question != null && !question.trim().isEmpty() && positiveOption != null
            && !positiveOption.trim().isEmpty() && negativeOption != null && !negativeOption.trim().isEmpty();
    }
}
