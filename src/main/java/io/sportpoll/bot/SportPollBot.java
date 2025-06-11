package io.sportpoll.bot;

import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;
import org.telegram.telegrambots.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import io.sportpoll.bot.config.Config;
import io.sportpoll.bot.config.CustomAbilityToggle;
import io.sportpoll.bot.constants.Messages;
import io.sportpoll.bot.services.PollManager;
import io.sportpoll.bot.services.WeeklyPollScheduler;
import io.sportpoll.bot.utils.MessageUtils;
import io.sportpoll.bot.utils.ExceptionHandler;
import io.sportpoll.bot.persistance.DataStore;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.telegram.telegrambots.abilitybots.api.objects.Locality.*;
import static org.telegram.telegrambots.abilitybots.api.objects.Privacy.*;

public class SportPollBot extends AbilityBot {
    private final PollManager pollManager;
    private final ScheduledExecutorService scheduler;
    private final Config config;

    public SportPollBot(TelegramClient telegramClient, String botUsername) {
        this(telegramClient, botUsername, DataStore.getInstance().get(PollManager.class),
            DataStore.getInstance().get(WeeklyPollScheduler.class), Config.getInstance(),
            Executors.newScheduledThreadPool(1));
    }

    public SportPollBot(TelegramClient telegramClient, String botUsername, PollManager pollManager,
        WeeklyPollScheduler weeklyPollScheduler, Config config, ScheduledExecutorService scheduler) {
        super(telegramClient, botUsername, createDatabaseContext(botUsername), new CustomAbilityToggle());
        this.pollManager = pollManager;
        this.config = config;
        this.scheduler = scheduler;
        weeklyPollScheduler.initialize();
        onRegister();
        startBackgroundTasks();
    }

    private static DBContext createDatabaseContext(String botUsername) {
        String dbDir = getDatabaseDirectory();
        String dbPath = dbDir + File.separator + botUsername;

        DB db = DBMaker.fileDB(dbPath).fileMmapEnableIfSupported().closeOnJvmShutdown().transactionEnable().make();

        return new MapDBContext(db);
    }

    private static String getDatabaseDirectory() {
        String dbDir = System.getenv("BOT_DATABASE_DIR");
        if (dbDir == null || dbDir.trim().isEmpty()) {
            dbDir = System.getProperty("bot.database.dir");
        }
        if (dbDir == null || dbDir.trim().isEmpty()) {
            dbDir = isTestEnvironment() ? getTestDatabaseDirectory() : "data";
        }

        File dataDir = new File(dbDir);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        return dataDir.getAbsolutePath();
    }

    private static boolean isTestEnvironment() {
        return Thread.currentThread().getStackTrace().length > 0
            && java.util.Arrays.stream(Thread.currentThread().getStackTrace())
                .anyMatch(elem -> elem.getClassName().contains("Test") || elem.getClassName().contains("junit"));
    }

    private static String getTestDatabaseDirectory() {
        if (Thread.currentThread().getStackTrace().length > 0) {
            String testClass = java.util.Arrays.stream(Thread.currentThread().getStackTrace())
                .filter(elem -> elem.getClassName().contains("Test"))
                .findFirst()
                .map(StackTraceElement::getClassName)
                .orElse("");

            if (testClass.contains("Integration")) {
                return "target" + File.separator + "test-db-integration";
            } else if (testClass.contains("Comprehensive")) {
                return "target" + File.separator + "test-db-comprehensive";
            } else {
                return "target" + File.separator + "test-db";
            }
        }
        return "target" + File.separator + "test-db";
    }

    @Override
    public long creatorId() {
        return config.adminUserIds.getFirst();
    }

    public Ability start() {
        return Ability.builder()
            .name("start")
            .info("Start the bot for admin")
            .locality(ALL)
            .privacy(PUBLIC)
            .action(ctx -> {
                if (isAdmin(ctx.user().getId())) {
                    if (ctx.chatId() != config.targetGroupChatId) {
                        ExceptionHandler.handle(() -> MessageUtils.routeUpdate(ctx.update(), pollManager));
                    } else {
                        getSilent().send(Messages.GROUP_WELCOME, ctx.chatId());
                    }
                } else {
                    if (ctx.chatId() != config.targetGroupChatId) {
                        String welcomeMessage = String.format(Messages.USER_WELCOME, ctx.user().getFirstName());
                        getSilent().send(welcomeMessage, ctx.chatId());
                    }
                }
            })
            .build();
    }

    public Reply voteReply() {
        return Reply.of((bot, upd) -> ExceptionHandler.handle(() -> pollManager.handleVoteCommand(upd)),
            Flag.TEXT,
            upd -> upd.getMessage().getText().startsWith("/+")
                && upd.getMessage().getChatId().equals(config.targetGroupChatId));
    }

    public Reply revokeReply() {
        return Reply.of((bot, upd) -> ExceptionHandler.handle(() -> pollManager.handleRevokeCommand(upd)),
            Flag.TEXT,
            upd -> upd.getMessage().getText().startsWith("/-")
                && upd.getMessage().getChatId().equals(config.targetGroupChatId));
    }

    public Reply callbackQueryReply() {
        return Reply.of((bot, upd) -> ExceptionHandler.handle(() -> MessageUtils.routeUpdate(upd, pollManager)),
            Flag.CALLBACK_QUERY);
    }

    public Reply adminMessageReply() {
        return Reply.of((bot, upd) -> ExceptionHandler.handle(() -> MessageUtils.routeUpdate(upd, pollManager)),
            Flag.TEXT,
            upd -> isAdmin(upd.getMessage().getFrom().getId())
                && upd.getMessage().getChatId() != config.targetGroupChatId
                && !upd.getMessage().getText().startsWith("/"));
    }

    public boolean isAdmin(long userId) {
        return config.adminUserIds.contains(userId);
    }

    private void startBackgroundTasks() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                pollManager.checkMondayClose();
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }, 0, 12, TimeUnit.HOURS);
    }

}
