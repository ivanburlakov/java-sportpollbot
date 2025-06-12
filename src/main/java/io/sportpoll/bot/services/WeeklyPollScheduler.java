package io.sportpoll.bot.services;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import io.sportpoll.bot.persistance.DataStore;
import io.sportpoll.bot.commands.PollCommand;
import io.sportpoll.bot.config.WeeklyPollConfig;

public class WeeklyPollScheduler implements Serializable {
    private transient WeeklyPollConfig config;
    private transient PollManager pollManager;
    private transient ScheduledExecutorService scheduler;
    private transient Supplier<Double> randomProvider;
    private transient Supplier<LocalDateTime> timeProvider;
    private Duration randomWindow = Duration.ofHours(1);

    public WeeklyPollScheduler() {
        this(null, null, null, null, null);
    }

    public WeeklyPollScheduler(WeeklyPollConfig config, PollManager pollManager, Supplier<LocalDateTime> timeProvider,
        Supplier<Double> randomProvider, ScheduledExecutorService scheduler) {
        this.config = config;
        this.pollManager = pollManager;
        this.timeProvider = timeProvider != null ? timeProvider : () -> LocalDateTime.now();
        this.randomProvider = randomProvider != null ? randomProvider : () -> ThreadLocalRandom.current().nextDouble();
        this.scheduler = scheduler != null ? scheduler : Executors.newScheduledThreadPool(1);
    }

    public void initialize() {
        scheduleNextPoll();
    }

    public WeeklyPollConfig getConfig() {
        if (config == null) config = DataStore.getInstance().get(WeeklyPollConfig.class);
        return config;
    }

    public void setRandomProvider(Supplier<Double> provider) {
        randomProvider = provider;
    }
    public void setTimeProvider(Supplier<LocalDateTime> provider) {
        timeProvider = provider;
    }
    public void setRandomWindow(Duration window) {
        randomWindow = window;
    }

    private PollManager getPollManager() {
        if (pollManager == null) pollManager = DataStore.getInstance().get(PollManager.class);
        System.out.println("WeeklyPollScheduler.getPollManager: returning " + pollManager + " (hasActivePoll: "
            + pollManager.hasActivePoll() + ")");
        return pollManager;
    }

    public void updateConfig(WeeklyPollConfig newConfig) {
        WeeklyPollConfig config = getConfig();
        config.setQuestion(newConfig.getQuestion());
        config.setPositiveOption(newConfig.getPositiveOption());
        config.setNegativeOption(newConfig.getNegativeOption());
        config.setTargetVotes(newConfig.getTargetVotes());
        config.setDayOfWeek(newConfig.getDayOfWeek());
        config.setStartTime(newConfig.getStartTime());
        config.setEnabled(newConfig.isEnabled());
        reschedule();
    }

    public void setEnabled(boolean enabled) {
        getConfig().setEnabled(enabled);
        if (enabled) reschedule();
        else if (scheduler != null) scheduler.shutdown();
    }

    private void reschedule() {
        if (scheduler != null) scheduler.shutdown();
        scheduler = Executors.newScheduledThreadPool(1);
        scheduleNextPoll();
    }

    public void runScheduledPollCheck() {
        if (!getConfig().isEnabled() || getPollManager().hasActivePoll()) return;
        LocalDateTime now = timeProvider.get();
        if (isTimeWithinScheduledWindow(now)) {
            createWeeklyPoll();
        }
    }

    private boolean isTimeWithinScheduledWindow(LocalDateTime now) {
        WeeklyPollConfig config = getConfig();
        LocalDateTime thisWeekScheduledTime = now.with(config.getDayOfWeek())
            .withHour(config.getStartTime().getHour())
            .withMinute(config.getStartTime().getMinute())
            .withSecond(config.getStartTime().getSecond())
            .withNano(0);
        LocalDateTime windowStart = thisWeekScheduledTime;
        LocalDateTime windowEnd = thisWeekScheduledTime.plus(randomWindow);
        return (now.isAfter(windowStart) || now.isEqual(windowStart))
            && (now.isBefore(windowEnd) || now.isEqual(windowEnd));
    }

    private void scheduleNextPoll() {
        if (!getConfig().isEnabled()) return;
        LocalDateTime now = timeProvider.get();
        LocalDateTime nextPollTime = calculateNextPollTime(now);
        long delaySeconds = java.time.temporal.ChronoUnit.SECONDS.between(now, nextPollTime);
        if (delaySeconds <= 0) {
            runScheduledPollCheck();
            return;
        }
        scheduler.schedule(() -> {
            try {
                runScheduledPollCheck();
            } catch (Exception e) {
                System.err.println("Error in scheduled poll check: " + e.getMessage());
            }
            scheduleNextPoll();
        }, delaySeconds, TimeUnit.SECONDS);
    }

    public LocalDateTime calculateNextPollTime(LocalDateTime from, LocalDateTime exactStartTime, Duration randomWindow,
        double randomFactor) {
        LocalDateTime candidate = exactStartTime;
        if (candidate.isBefore(from) || candidate.isEqual(from)) candidate = candidate.plusWeeks(1);
        long windowMillis = randomWindow.toMillis();
        long randomOffsetMillis = (long) (randomFactor * windowMillis);
        return candidate.plus(randomOffsetMillis, java.time.temporal.ChronoUnit.MILLIS);
    }

    public LocalDateTime calculateNextPollTime(LocalDateTime from, java.time.DayOfWeek dayOfWeek,
        java.time.LocalTime startTime, Duration randomWindow, double randomFactor) {
        LocalDateTime candidate = from.with(dayOfWeek)
            .withHour(startTime.getHour())
            .withMinute(startTime.getMinute())
            .withSecond(startTime.getSecond())
            .withNano(0);
        return calculateNextPollTime(from, candidate, randomWindow, randomFactor);
    }

    public LocalDateTime calculateNextPollTime(LocalDateTime from) {
        WeeklyPollConfig config = getConfig();
        return calculateNextPollTime(from,
            config.getDayOfWeek(),
            config.getStartTime(),
            randomWindow,
            getRandomProvider().get());
    }

    private Supplier<Double> getRandomProvider() {
        if (randomProvider == null) randomProvider = () -> ThreadLocalRandom.current().nextDouble();
        return randomProvider;
    }

    private void createWeeklyPoll() {
        WeeklyPollConfig currentConfig = getConfig();
        PollCommand cmd = new PollCommand(getPollManager());
        PollCommand.PollCreationResult result = cmd.createPoll(currentConfig.getQuestion(),
            currentConfig.getPositiveOption(),
            currentConfig.getNegativeOption(),
            currentConfig.getTargetVotes());
        if (!result.success()) {
            System.err.println("WeeklyPollScheduler: Failed to create weekly poll - " + result.message());
        }
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        scheduler = Executors.newScheduledThreadPool(1);
        randomProvider = () -> ThreadLocalRandom.current().nextDouble();
        timeProvider = () -> LocalDateTime.now();
    }
}
