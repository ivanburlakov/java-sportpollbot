package io.sportpoll.bot.ui;

public enum Menu {
    MAIN_MENU("main:menu"), CREATE_POLL("main:create"), WEEKLY_SETTINGS("main:weekly"), WEEKLY_CONFIG("weekly:config"),
    CONFIRM_POLL("poll:confirm"), EDIT_POLL_QUESTION("poll:edit:question"), EDIT_POLL_POSITIVE("poll:edit:positive"),
    EDIT_POLL_NEGATIVE("poll:edit:negative"), EDIT_POLL_VOTES("poll:edit:votes");

    private final String id;

    Menu(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static Menu fromId(String id) {
        for (Menu menu : values()) {
            if (menu.id.equals(id)) {
                return menu;
            }
        }
        return null;
    }
}
