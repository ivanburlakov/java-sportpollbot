package io.sportpoll.bot.services;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import io.sportpoll.bot.config.Config;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.polls.StopPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.polls.input.InputPollOption;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import io.sportpoll.bot.models.Vote;
import io.sportpoll.bot.constants.Messages;
import io.sportpoll.bot.utils.CommandUtils;
import io.sportpoll.bot.utils.MessageUtils;

public class PollManager implements Serializable {
    private int targetVotes;
    private Map<User, Integer> directVoters;
    private List<Vote> externalVotes;
    private Integer telegramMessageId;
    private Integer statusMessageId;
    private boolean isActive = false;
    private transient long targetGroupChatId;

    public PollManager() {
        this.targetGroupChatId = Config.getInstance().targetGroupChatId;
    }

    public PollManager(long targetGroupChatId) {
        this.targetGroupChatId = targetGroupChatId;
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.targetGroupChatId = Config.getInstance().targetGroupChatId;
    }

    public void createAndPostPoll(String question, String positiveOption, String negativeOption, int targetVotes,
        Update update) throws TelegramApiException {
        if (isActive) resetPoll();
        this.targetVotes = targetVotes;
        this.directVoters = new HashMap<>();
        this.externalVotes = new ArrayList<>();
        this.telegramMessageId = null;
        this.statusMessageId = null;
        this.isActive = true;
        SendPoll sendPoll = SendPoll.builder()
            .chatId(targetGroupChatId)
            .question(question)
            .options(Arrays.asList(InputPollOption.builder().text(positiveOption).build(),
                InputPollOption.builder().text(negativeOption).build()))
            .isAnonymous(false)
            .build();
        var result = TelegramClientService.getInstance().execute(sendPoll);
        this.telegramMessageId = result.getMessageId();
        createStatusMessage();
        MessageUtils.sendMessage(String.format(Messages.SUCCESS, Messages.POLL_CREATED),
            update.getMessage().getChatId());

    }

    public boolean hasActivePoll() {
        return isActive;
    }

    private void resetPoll() throws TelegramApiException {
        if (statusMessageId != null) unpinStatusMessage();
        this.isActive = false;
        this.directVoters = null;
        this.externalVotes = null;
        this.telegramMessageId = null;
        this.statusMessageId = null;
    }

    public void resetPollPublic() throws TelegramApiException {
        resetPoll();
    }
    public void addExternalVote(String[] names, int voteCount, Update update) throws TelegramApiException {
        long requesterId = update.getMessage().getFrom().getId();
        String requesterName = update.getMessage().getFrom().getFirstName();
        int currentVotes = getPositiveVotes();
        int remaining = targetVotes - currentVotes;
        if (remaining <= 0) {
            MessageUtils.sendMessage(Messages.POLL_CLOSED, targetGroupChatId);
            return;
        }
        if (voteCount > remaining) {
            MessageUtils.sendError(String.format(Messages.TOO_MANY_VOTES, voteCount, remaining), targetGroupChatId);
            return;
        }
        if (names != null && names.length > 0) {
            for (String name : names)
                externalVotes.add(new Vote(requesterId, requesterName, name.trim()));
        } else {
            for (int i = 0; i < voteCount; i++)
                externalVotes.add(new Vote(requesterId, requesterName));
        }
        String voteText = names == null
            ? (voteCount == 1 ? Messages.ANONYMOUS_VOTE_SINGLE
                : String.format(Messages.ANONYMOUS_VOTES_MULTIPLE, voteCount))
            : (voteCount == 1 ? String.format(Messages.NAMED_VOTE_SINGLE, String.join(", ", names))
                : String.format(Messages.NAMED_VOTES_MULTIPLE, String.join(", ", names)));
        MessageUtils.sendMessage(String.format(Messages.VOTE_ADDED, voteText), targetGroupChatId);
        checkCompletion();
    }
    private void ensureCollections() {
        if (directVoters == null) directVoters = new HashMap<>();
        if (externalVotes == null) externalVotes = new ArrayList<>();
    }
    public synchronized int getPositiveVotes() {
        ensureCollections();
        return (int) directVoters.entrySet().stream().filter(entry -> entry.getValue() == 0).count()
            + externalVotes.size();
    }
    public void checkCompletion() throws TelegramApiException {
        if (getPositiveVotes() >= targetVotes) {
            closeDirectPoll();
            this.isActive = false;
            updateStatusMessage();
            MessageUtils.sendMessage(Messages.POLL_COMPLETION_MESSAGE, targetGroupChatId);
        } else updateStatusMessage();
    }

    public void handleVoteCommand(Update update) throws TelegramApiException {

        long chatId = update.getMessage().getChatId();
        if (!isActive) {
            MessageUtils.sendError(Messages.NO_ACTIVE_POLL, chatId);
            return;
        }
        String[] args = CommandUtils.parseArguments(update.getMessage().getText());
        if (args.length == 0) addExternalVote(null, 1, update);
        else if (args.length == 1 && args[0].matches("\\d+")) {
            int count = Integer.parseInt(args[0]);
            if (count > 12) throw new IllegalArgumentException(Messages.DEFAULT_ERROR);
            addExternalVote(null, count, update);
        } else addExternalVote(args, args.length, update);
    }

    public void handleDirectVote(Update update) throws TelegramApiException {
        if (!isActive) return;
        User user = update.getPollAnswer().getUser();
        List<Integer> optionIds = update.getPollAnswer().getOptionIds();
        if (optionIds.isEmpty()) directVoters.remove(user);
        else directVoters.put(user, optionIds.get(0));
        checkCompletion();
    }

    public void closeCurrentPoll(Update update) throws TelegramApiException {
        if (!isActive) {
            MessageUtils.acknowledgeCallback(update, Messages.NO_ACTIVE_POLL);
            return;
        }
        closeDirectPoll();
        resetPoll();
        MessageUtils.acknowledgeCallback(update, Messages.POLL_CLOSED);
    }

    public boolean closeCurrentPollSilent() throws TelegramApiException {
        if (!isActive) {
            return false;
        }
        closeDirectPoll();
        resetPoll();
        return true;
    }

    public void checkMondayClose() throws TelegramApiException {
        if (isActive) {
            closeDirectPoll();
            resetPoll();
        }
    }

    public void handleRevoke(Update update) throws TelegramApiException {
        long userId = update.getMessage().getFrom().getId();
        handleRevoke(userId, targetGroupChatId);
    }

    public void handleRevokeCommand(Update update) throws TelegramApiException {
        String messageText = update.getMessage().getText();
        String[] parts = messageText.split("\\s+");
        long userId = update.getMessage().getFrom().getId();
        long chatId = update.getMessage().getChatId();
        if (parts.length == 1) handleRevoke(userId, chatId);
        else if (parts.length == 2) {
            try {
                int voteNumber = Integer.parseInt(parts[1]);
                revokeVoteByNumber(voteNumber, userId, chatId);
            } catch (NumberFormatException e) {
                MessageUtils.sendError(Messages.REVOKE_USAGE, chatId);
            }
        } else MessageUtils.sendError(Messages.REVOKE_USAGE, chatId);
    }

    public void handleRevoke(long userId, long chatId) throws TelegramApiException {
        if (!isActive) {
            MessageUtils.sendError(Messages.NO_ACTIVE_POLL, chatId);
            return;
        }
        boolean found = false;
        for (int i = externalVotes.size() - 1; i >= 0; i--) {
            Vote vote = externalVotes.get(i);
            if (vote.getRequesterId() == userId) {
                externalVotes.remove(i);
                found = true;
                break;
            }
        }
        if (found) {
            MessageUtils.sendMessage(String.format(Messages.SUCCESS, Messages.VOTE_REVOKED), chatId);
            updateStatusMessage();
        } else MessageUtils.sendError(Messages.NO_VOTES_TO_REVOKE, chatId);
    }

    public void closeDirectPoll() throws TelegramApiException {
        if (!isActive) {
            MessageUtils.sendError(Messages.NO_ACTIVE_POLL, targetGroupChatId);
            return;
        }
        if (telegramMessageId == null) return;
        StopPoll stopPoll = StopPoll.builder().chatId(targetGroupChatId).messageId(telegramMessageId).build();
        TelegramClientService.getInstance().execute(stopPoll);
    }

    public void initializePoll(int targetVotes, Integer telegramMessageId) throws TelegramApiException {
        this.targetVotes = targetVotes;
        this.directVoters = new HashMap<>();
        this.externalVotes = new ArrayList<>();
        this.telegramMessageId = telegramMessageId;
        this.statusMessageId = null;
        this.isActive = true;
        createStatusMessage();
    }

    public void revokeVoteByNumber(int voteNumber, long requesterId, long chatId) throws TelegramApiException {
        if (!isActive) {
            MessageUtils.sendError(Messages.NO_ACTIVE_POLL, chatId);
            return;
        }
        if (voteNumber < 1) {
            MessageUtils.sendError(Messages.INVALID_VOTE_NUMBER, chatId);
            return;
        }
        boolean isAdmin = Config.getInstance().adminUserIds.contains(requesterId);
        int currentNumber = 1;
        for (var entry : directVoters.entrySet()) {
            if (entry.getValue() == 0) {
                if (currentNumber == voteNumber) {
                    if (isAdmin || entry.getKey().getId() == requesterId) {
                        String voterName = entry.getKey().getFirstName();
                        directVoters.remove(entry.getKey());
                        String message = isAdmin && entry.getKey().getId() != requesterId
                            ? String.format(Messages.ADMIN_REVOKED_DIRECT_VOTE, voteNumber, voterName)
                            : String.format(Messages.REVOKED_DIRECT_VOTE, voteNumber, voterName);
                        MessageUtils.sendMessage(String.format(Messages.SUCCESS, message), chatId);
                        updateStatusMessage();
                    } else {
                        MessageUtils.sendError(Messages.PERMISSION_DENIED_REVOKE, chatId);
                    }
                    return;
                }
                currentNumber++;
            }
        }
        for (int i = 0; i < externalVotes.size(); i++) {
            if (currentNumber == voteNumber) {
                Vote vote = externalVotes.get(i);
                if (isAdmin || vote.getRequesterId() == requesterId) {
                    String voterName = vote.isAnonymous() ? Messages.ANONYMOUS_VOTER : vote.getVoterName();
                    String requesterName = vote.getRequesterName();
                    externalVotes.remove(i);
                    String message = isAdmin && vote.getRequesterId() != requesterId
                        ? String.format(Messages.ADMIN_REVOKED_EXTERNAL_VOTE, voteNumber, voterName, requesterName)
                        : String.format(Messages.REVOKED_EXTERNAL_VOTE, voteNumber, voterName);
                    MessageUtils.sendMessage(String.format(Messages.SUCCESS, message), chatId);
                    updateStatusMessage();
                } else {
                    MessageUtils.sendError(Messages.PERMISSION_DENIED_REVOKE, chatId);
                }
                return;
            }
            currentNumber++;
        }
        MessageUtils.sendError(String.format(Messages.VOTE_NOT_FOUND_BY_NUMBER, voteNumber), chatId);
    }

    private void createStatusMessage() throws TelegramApiException {
        String statusText = buildStatusMessage();
        SendMessage statusMessage = SendMessage.builder()
            .chatId(targetGroupChatId)
            .text(statusText)
            .parseMode("HTML")
            .disableNotification(true)
            .build();
        var result = TelegramClientService.getInstance().execute(statusMessage);
        this.statusMessageId = result.getMessageId();
        pinStatusMessage();
    }

    private void updateStatusMessage() throws TelegramApiException {
        if (statusMessageId == null) return;
        try {
            String statusText = buildStatusMessage();
            EditMessageText editMessage = EditMessageText.builder()
                .chatId(targetGroupChatId)
                .messageId(statusMessageId)
                .text(statusText)
                .parseMode("HTML")
                .build();
            TelegramClientService.getInstance().execute(editMessage);
        } catch (TelegramApiException e) {
            if (e.getMessage().contains("message not found") || e.getMessage().contains("message to edit not found")
                || e.getMessage().contains("MESSAGE_ID_INVALID")) recreateStatusMessage();
        }
    }

    private void recreateStatusMessage() throws TelegramApiException {
        if (statusMessageId != null) {
            DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(targetGroupChatId)
                .messageId(statusMessageId)
                .build();
            TelegramClientService.getInstance().execute(deleteMessage);
        }
        createStatusMessage();
    }

    private String buildStatusMessage() {
        StringBuilder status = new StringBuilder();
        status.append(Messages.STATUS_HEADER);
        int currentVotes = getPositiveVotes();
        boolean isCompleted = currentVotes >= targetVotes;
        if (isCompleted) status.append(Messages.STATUS_POLL_COMPLETED);
        else status.append(Messages.STATUS_POLL_ACTIVE);
        status.append(String.format(Messages.STATUS_TARGET, targetVotes));
        if (!isCompleted) {
            int remaining = targetVotes - currentVotes;
            status.append(String.format(Messages.STATUS_REMAINING, remaining));
            status.append(Messages.STATUS_HOW_TO_VOTE);
            status.append(Messages.STATUS_USE_POLL_ABOVE);
            status.append(Messages.STATUS_OR_USE_COMMANDS);
            status.append(Messages.STATUS_COMMAND_PLUS);
            status.append(Messages.STATUS_COMMAND_PLUS_NUMBER);
            status.append(Messages.STATUS_COMMAND_PLUS_NAMES);
            status.append(Messages.STATUS_COMMAND_MINUS);
        }
        status.append(Messages.STATUS_VOTE_LIST);
        appendVoteListSimple(status);
        return status.toString();
    }

    private void appendVoteListSimple(StringBuilder status) {
        int voteNumber = 1;
        for (var entry : directVoters.entrySet())
            if (entry.getValue() == 0)
                status.append(String.format("%d. %s\n", voteNumber++, entry.getKey().getFirstName()));
        for (Vote vote : externalVotes) {
            String voterName = vote.isAnonymous() ? Messages.ANONYMOUS_VOTER : vote.getVoterName();
            status.append(String.format("%d. %s (%s)\n",
                voteNumber++,
                voterName,
                String.format(Messages.INVITED, vote.getRequesterName())));
        }
    }

    private void pinStatusMessage() throws TelegramApiException {
        if (statusMessageId == null) return;
        var pinMessage = org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage.builder()
            .chatId(targetGroupChatId)
            .messageId(statusMessageId)
            .disableNotification(true)
            .build();
        TelegramClientService.getInstance().execute(pinMessage);
    }

    private void unpinStatusMessage() throws TelegramApiException {
        if (statusMessageId == null) return;
        var unpinMessage = org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage.builder()
            .chatId(targetGroupChatId)
            .messageId(statusMessageId)
            .build();
        TelegramClientService.getInstance().execute(unpinMessage);
    }
}
