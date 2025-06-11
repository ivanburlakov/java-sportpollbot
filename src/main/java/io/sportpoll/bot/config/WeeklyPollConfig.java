package io.sportpoll.bot.config;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class WeeklyPollConfig implements Serializable {
    private String question;
    private String positiveOption;
    private String negativeOption;
    private int targetVotes;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private boolean enabled;

    public WeeklyPollConfig() {
        this.question = "В четвер в 20:00 де завжди?";
        this.positiveOption = "Так, граю!";
        this.negativeOption = "Ні, не можу";
        this.targetVotes = 12;
        this.dayOfWeek = DayOfWeek.THURSDAY;
        this.startTime = LocalTime.of(13, 0);
        this.enabled = true;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getPositiveOption() {
        return positiveOption;
    }

    public void setPositiveOption(String positiveOption) {
        this.positiveOption = positiveOption;
    }

    public String getNegativeOption() {
        return negativeOption;
    }

    public void setNegativeOption(String negativeOption) {
        this.negativeOption = negativeOption;
    }

    public int getTargetVotes() {
        return targetVotes;
    }

    public void setTargetVotes(int targetVotes) {
        this.targetVotes = targetVotes;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setDayOfWeek(int day) {
        if (day < 1 || day > 7) throw new IllegalArgumentException("День повинен бути від 1 до 7");
        setDayOfWeek(DayOfWeek.of(day));
    }

    public void setStartTime(String time) {
        String[] parts = time.split(":");
        if (parts.length != 2) throw new IllegalArgumentException("Час повинен бути в форматі ГГ:ХХ");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        if (hours < 0 || hours > 23) throw new IllegalArgumentException("Години повинні бути від 0 до 23");
        if (minutes < 0 || minutes > 59) throw new IllegalArgumentException("Хвилини повинні бути від 0 до 59");
        setStartTime(LocalTime.of(hours, minutes));
    }
}
