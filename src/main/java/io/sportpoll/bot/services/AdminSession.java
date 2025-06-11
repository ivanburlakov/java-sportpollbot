package io.sportpoll.bot.services;

import io.sportpoll.bot.persistance.DataStore;
import io.sportpoll.bot.ui.Pages;
import io.sportpoll.bot.commands.PollCommand;
import io.sportpoll.bot.config.WeeklyPollConfig;
import io.sportpoll.bot.utils.ExceptionHandler;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class AdminSession {
    private final long chatId;
    private final PollManager pollManager;
    private final WeeklyPollScheduler weeklyScheduler;
    private PollData currentPollData;
    private Integer lastMenuMessageId;
    private final org.telegram.telegrambots.meta.generics.TelegramClient telegramClient;

    public record PollData(String question, String positiveOption, String negativeOption, int targetVotes) {
        public PollData withQuestion(String question) {
            return new PollData(question, positiveOption, negativeOption, targetVotes);
        }
        public PollData withPositiveOption(String positiveOption) {
            return new PollData(question, positiveOption, negativeOption, targetVotes);
        }
        public PollData withNegativeOption(String negativeOption) {
            return new PollData(question, positiveOption, negativeOption, targetVotes);
        }
        public PollData withTargetVotes(int targetVotes) {
            return new PollData(question, positiveOption, negativeOption, targetVotes);
        }
    }

    private enum EditTarget {
        QUESTION("📝 Введіть нове питання:"), POSITIVE("✅ Введіть новий варіант \"за\":"),
        NEGATIVE("❌ Введіть новий варіант \"проти\":"), VOTES("🎯 Введіть нову ціль голосів:"),
        WEEKLY_QUESTION("📝 Введіть нове тижневе питання:"),
        WEEKLY_POSITIVE("✅ Введіть новий тижневий варіант \"за\":"),
        WEEKLY_NEGATIVE("❌ Введіть новий тижневий варіант \"проти\":"),
        WEEKLY_VOTES("🎯 Введіть нову тижневу ціль голосів:"), WEEKLY_DAY("📅 Виберіть день тижня (1-7):"),
        WEEKLY_TIME("⏰ Введіть час (ГГ:ХХ):");

        private final String prompt;

        EditTarget(String prompt) {
            this.prompt = prompt;
        }
    }

    private EditTarget currentEditTarget;

    public AdminSession(Update update, org.telegram.telegrambots.meta.generics.TelegramClient telegramClient,
        PollManager pollManager) {
        this.chatId = update.hasMessage() ? update.getMessage().getChatId()
            : update.getCallbackQuery().getMessage().getChatId();
        this.pollManager = pollManager;
        this.weeklyScheduler = DataStore.getInstance().get(WeeklyPollScheduler.class);
        this.telegramClient = telegramClient;
    }

    public AdminSession(Update update, org.telegram.telegrambots.meta.generics.TelegramClient telegramClient) {
        this(update, telegramClient, DataStore.getInstance().get(PollManager.class));
    }

    public AdminSession(Update update, PollManager pollManager) {
        this(update, TelegramClientService.getInstance(), pollManager);
    }

    public void handleUpdate(Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            handleCallback(callbackData, update);
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            if ("/start".equals(text)) {
                showMainMenu(update);
            } else if (currentEditTarget != null) {
                handleEdit(text, update);
            }
        }
    }

    private void showMainMenu(Update update) {
        boolean hasActivePoll = pollManager.hasActivePoll();
        sendPage(Pages.MainMenuPage.getText(hasActivePoll), Pages.MainMenuPage.getKeyboard(hasActivePoll));
    }

    private void showPollMenu(Update update) {
        WeeklyPollConfig config = weeklyScheduler.getConfig();
        if (currentPollData == null) {
            currentPollData = new PollData(config.getQuestion(), config.getPositiveOption(), config.getNegativeOption(),
                config.getTargetVotes());
        }
        String text = Pages.CreatePollPage.getText(currentPollData.question,
            currentPollData.positiveOption,
            currentPollData.negativeOption,
            currentPollData.targetVotes);
        sendPage(text, Pages.CreatePollPage.getKeyboard());
    }

    private void showWeeklyMenu(Update update) {
        var config = weeklyScheduler.getConfig();
        String dayShort = switch (config.getDayOfWeek()) {
            case MONDAY -> "Пн";
            case TUESDAY -> "Вт";
            case WEDNESDAY -> "Ср";
            case THURSDAY -> "Чт";
            case FRIDAY -> "Пт";
            case SATURDAY -> "Сб";
            case SUNDAY -> "Нд";
        };
        String timeStr = String.format("%02d:00", config.getStartTime().getHour());
        String text = Pages.WeeklySettingsPage.getText(config.getQuestion(),
            config.getPositiveOption(),
            config.getNegativeOption(),
            config.getTargetVotes(),
            timeStr,
            dayShort,
            config.isEnabled());
        sendPage(text, Pages.WeeklySettingsPage.getKeyboard(config.isEnabled()));
    }

    private void handleCallback(String callbackData, Update update) throws TelegramApiException {
        String[] parts = callbackData.split(":");
        switch (parts[0]) {
            case "main" -> {
                switch (parts[1]) {
                    case "create" -> showPollMenu(update);
                    case "weekly" -> showWeeklyMenu(update);
                    case "menu" -> showMainMenu(update);
                    case "close" -> {
                        pollManager.closeCurrentPoll(update);
                        showMainMenu(update);
                    }
                }
            }
            case "poll" -> {
                switch (parts[1]) {
                    case "edit" -> {
                        String backTarget = "poll:menu";
                        switch (parts[2]) {
                            case "question" -> startEdit(EditTarget.QUESTION,
                                currentPollData.question(),
                                backTarget,
                                update);
                            case "positive" -> startEdit(EditTarget.POSITIVE,
                                currentPollData.positiveOption(),
                                backTarget,
                                update);
                            case "negative" -> startEdit(EditTarget.NEGATIVE,
                                currentPollData.negativeOption(),
                                backTarget,
                                update);
                            case "votes" -> startEdit(EditTarget.VOTES,
                                String.valueOf(currentPollData.targetVotes()),
                                backTarget,
                                update);
                        }
                    }
                    case "confirm" -> handlePollConfirmation(update);
                    case "menu" -> showPollMenu(update);
                }
            }
            case "weekly" -> {
                var config = weeklyScheduler.getConfig();
                if ("day".equals(parts[1]) && parts.length == 3) {
                    int day = Integer.parseInt(parts[2]);
                    config.setDayOfWeek(day);
                    weeklyScheduler.updateConfig(config);
                    showWeeklyMenu(update);
                } else if ("day".equals(parts[1])) {
                    currentEditTarget = EditTarget.WEEKLY_DAY;
                    sendPage(Pages.WeeklyDayPage.getText(), Pages.WeeklyDayPage.getKeyboard());
                } else if ("time".equals(parts[1])) {
                    currentEditTarget = EditTarget.WEEKLY_TIME;
                    sendPage(Pages.WeeklyTimePage.getText(), Pages.WeeklyTimePage.getKeyboard());
                } else if ("config".equals(parts[1])) {
                    String backTarget = "weekly:menu";
                    switch (parts[2]) {
                        case "question" -> startEdit(EditTarget.WEEKLY_QUESTION,
                            config.getQuestion(),
                            backTarget,
                            update);
                        case "positive" -> startEdit(EditTarget.WEEKLY_POSITIVE,
                            config.getPositiveOption(),
                            backTarget,
                            update);
                        case "negative" -> startEdit(EditTarget.WEEKLY_NEGATIVE,
                            config.getNegativeOption(),
                            backTarget,
                            update);
                        case "votes" -> startEdit(EditTarget.WEEKLY_VOTES,
                            String.valueOf(config.getTargetVotes()),
                            backTarget,
                            update);
                        case "day" -> {
                            currentEditTarget = EditTarget.WEEKLY_DAY;
                            sendPage(Pages.WeeklyDayPage.getText(), Pages.WeeklyDayPage.getKeyboard());
                        }
                        case "time" -> {
                            currentEditTarget = EditTarget.WEEKLY_TIME;
                            sendPage(Pages.WeeklyTimePage.getText(), Pages.WeeklyTimePage.getKeyboard());
                        }
                        case "toggle" -> {
                            config.setEnabled(!config.isEnabled());
                            weeklyScheduler.updateConfig(config);
                            showWeeklyMenu(update);
                        }
                    }
                } else if ("menu".equals(parts[1])) {
                    showWeeklyMenu(update);
                }
            }
        }
    }

    private void startEdit(EditTarget target, String currentValue, String backCallbackTarget, Update update) {
        currentEditTarget = target;
        String text = Pages.EditPage.getText(target.prompt, currentValue);
        InlineKeyboardMarkup keyboard = Pages.EditPage.getKeyboard(backCallbackTarget);
        sendPage(text, keyboard);
    }

    private void handleEdit(String newValue, Update update) {
        if (currentEditTarget == null) return;
        try {
            switch (currentEditTarget) {
                case QUESTION -> {
                    currentPollData = currentPollData.withQuestion(newValue);
                    showPollMenu(update);
                    deleteUserMessage(update);
                }
                case POSITIVE -> {
                    currentPollData = currentPollData.withPositiveOption(newValue);
                    showPollMenu(update);
                    deleteUserMessage(update);
                }
                case NEGATIVE -> {
                    currentPollData = currentPollData.withNegativeOption(newValue);
                    showPollMenu(update);
                    deleteUserMessage(update);
                }
                case VOTES -> {
                    int votes = Integer.parseInt(newValue);
                    if (votes <= 0) throw new IllegalArgumentException("Votes must be positive");
                    currentPollData = currentPollData.withTargetVotes(votes);
                    showPollMenu(update);
                    deleteUserMessage(update);
                }
                case WEEKLY_QUESTION -> {
                    var config = weeklyScheduler.getConfig();
                    config.setQuestion(newValue);
                    weeklyScheduler.updateConfig(config);
                    showWeeklyMenu(update);
                    deleteUserMessage(update);
                }
                case WEEKLY_POSITIVE -> {
                    var config = weeklyScheduler.getConfig();
                    config.setPositiveOption(newValue);
                    weeklyScheduler.updateConfig(config);
                    showWeeklyMenu(update);
                    deleteUserMessage(update);
                }
                case WEEKLY_NEGATIVE -> {
                    var config = weeklyScheduler.getConfig();
                    config.setNegativeOption(newValue);
                    weeklyScheduler.updateConfig(config);
                    showWeeklyMenu(update);
                    deleteUserMessage(update);
                }
                case WEEKLY_VOTES -> {
                    var config = weeklyScheduler.getConfig();
                    int votes = Integer.parseInt(newValue);
                    if (votes <= 0) throw new IllegalArgumentException("Votes must be positive");
                    config.setTargetVotes(votes);
                    weeklyScheduler.updateConfig(config);
                    showWeeklyMenu(update);
                    deleteUserMessage(update);
                }
                case WEEKLY_DAY -> {
                    int day = Integer.parseInt(newValue);
                    if (day < 1 || day > 7) throw new IllegalArgumentException("Day must be between 1 and 7");
                    var config = weeklyScheduler.getConfig();
                    config.setDayOfWeek(day);
                    weeklyScheduler.updateConfig(config);
                    showWeeklyMenu(update);
                    deleteUserMessage(update);
                }
                case WEEKLY_TIME -> {
                    int hour = Integer.parseInt(newValue);
                    if (hour < 0 || hour > 23) throw new IllegalArgumentException("Hour must be between 0 and 23");
                    var config = weeklyScheduler.getConfig();
                    config.setStartTime(java.time.LocalTime.of(hour, 0));
                    weeklyScheduler.updateConfig(config);
                    showWeeklyMenu(update);
                    deleteUserMessage(update);
                }
            }
            currentEditTarget = null;
        } catch (IllegalArgumentException e) {
            var errorMsg = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text("❌ Некоректне значення. Спробуйте ще раз.")
                .build();
            sendPage(errorMsg.getText(), null);
        }
    }

    private void handlePollConfirmation(Update update) {
        var pollCommand = new PollCommand(pollManager);
        pollCommand.createPoll(currentPollData.question(),
            currentPollData.positiveOption(),
            currentPollData.negativeOption(),
            currentPollData.targetVotes());
        currentPollData = null;
        showMainMenu(update);
    }

    private void sendPage(String text, InlineKeyboardMarkup keyboard) {
        try {
            if (lastMenuMessageId != null) {
                var editMessage = org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText.builder()
                    .chatId(String.valueOf(chatId))
                    .messageId(lastMenuMessageId)
                    .text(text)
                    .parseMode("HTML")
                    .replyMarkup(keyboard)
                    .build();
                telegramClient.execute(editMessage);
            } else {
                var sendMessage = SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text(text)
                    .parseMode("HTML")
                    .replyMarkup(keyboard)
                    .build();
                var sentMessage = telegramClient.execute(sendMessage);
                if (sentMessage != null) {
                    lastMenuMessageId = sentMessage.getMessageId();
                }
            }
        } catch (TelegramApiException e) {
            System.err.println("Failed to send page: " + e.getMessage());
            if (lastMenuMessageId != null) {
                lastMenuMessageId = null;
                try {
                    var sendMessage = SendMessage.builder()
                        .chatId(String.valueOf(chatId))
                        .text(text)
                        .parseMode("HTML")
                        .replyMarkup(keyboard)
                        .build();
                    var sentMessage = telegramClient.execute(sendMessage);
                    if (sentMessage != null) {
                        lastMenuMessageId = sentMessage.getMessageId();
                    }
                } catch (TelegramApiException fallbackException) {
                    System.err.println("Fallback send also failed: " + fallbackException.getMessage());
                }
            }
        }
    }

    private void deleteUserMessage(Update update) {
        if (update.hasMessage()) {
            var delete = org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage.builder()
                .chatId(String.valueOf(chatId))
                .messageId(update.getMessage().getMessageId())
                .build();
            ExceptionHandler.handle(() -> telegramClient.execute(delete));
        }
    }
}
