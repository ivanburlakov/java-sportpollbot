package io.sportpoll.bot.ui;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import io.sportpoll.bot.constants.UIText;
import java.util.ArrayList;
import java.util.List;

public class Pages {
    public static class EditPage {
        public static String getText(String title, String currentValue) {
            return String.format("""
                %s

                %s

                %s""", title, String.format(UIText.EDIT_CURRENT_VALUE, currentValue), UIText.EDIT_ENTER_NEW_VALUE);
        }

        public static InlineKeyboardMarkup getKeyboard(String backTarget) {
            List<InlineKeyboardRow> rows = new ArrayList<>();
            InlineKeyboardRow row = new InlineKeyboardRow();
            row.add(InlineKeyboardButton.builder().text(UIText.BUTTON_CANCEL).callbackData(backTarget).build());
            rows.add(row);
            return InlineKeyboardMarkup.builder().keyboard(rows).build();
        }
    }

    public static class MainMenuPage {
        public static String getText(boolean hasActivePoll) {
            return UIText.MAIN_MENU_TITLE;
        }

        public static InlineKeyboardMarkup getKeyboard(boolean hasActivePoll) {
            List<InlineKeyboardRow> rows = new ArrayList<>();
            InlineKeyboardRow row1 = new InlineKeyboardRow();
            row1.add(
                InlineKeyboardButton.builder().text(UIText.BUTTON_CREATE_POLL).callbackData("main:create").build());
            row1.add(InlineKeyboardButton.builder().text(UIText.BUTTON_CLOSE_POLL).callbackData("main:close").build());
            rows.add(row1);
            InlineKeyboardRow row2 = new InlineKeyboardRow();
            row2.add(
                InlineKeyboardButton.builder().text(UIText.BUTTON_WEEKLY_SETTINGS).callbackData("main:weekly").build());
            rows.add(row2);
            return InlineKeyboardMarkup.builder().keyboard(rows).build();
        }
    }

    public static class CreatePollPage {
        public static String getText(String question, String positive, String negative, int votes) {
            return String
                .format(UIText.CREATE_POLL_TEMPLATE, UIText.CREATE_POLL_TITLE, question, positive, negative, votes);
        }

        public static InlineKeyboardMarkup getKeyboard() {
            List<InlineKeyboardRow> rows = new ArrayList<>();

            InlineKeyboardRow row1 = new InlineKeyboardRow();
            row1.add(InlineKeyboardButton.builder()
                .text(UIText.BUTTON_EDIT_QUESTION)
                .callbackData("poll:edit:question")
                .build());
            rows.add(row1);

            InlineKeyboardRow row2 = new InlineKeyboardRow();
            row2.add(InlineKeyboardButton.builder()
                .text(UIText.BUTTON_EDIT_POSITIVE)
                .callbackData("poll:edit:positive")
                .build());
            row2.add(InlineKeyboardButton.builder()
                .text(UIText.BUTTON_EDIT_NEGATIVE)
                .callbackData("poll:edit:negative")
                .build());
            rows.add(row2);

            InlineKeyboardRow row3 = new InlineKeyboardRow();
            row3.add(
                InlineKeyboardButton.builder().text(UIText.BUTTON_EDIT_TARGET).callbackData("poll:edit:votes").build());
            rows.add(row3);

            InlineKeyboardRow row4 = new InlineKeyboardRow();
            row4.add(InlineKeyboardButton.builder().text(UIText.BUTTON_CREATE).callbackData("poll:confirm").build());
            row4.add(InlineKeyboardButton.builder().text(UIText.BUTTON_CANCEL).callbackData("main:menu").build());
            rows.add(row4);

            return InlineKeyboardMarkup.builder().keyboard(rows).build();
        }
    }

    public static class WeeklySettingsPage {
        public static String getText(String question, String positive, String negative, int votes, String time,
            String day, boolean enabled) {
            String dayShort = UIText.getDayShort(day);
            String timeStr = UIText.WEEKLY_TIME_PREFIX + time.substring(0, 5);
            String status = enabled ? UIText.WEEKLY_STATUS_ENABLED : UIText.WEEKLY_STATUS_DISABLED;
            return String.format(UIText.WEEKLY_SETTINGS_TEMPLATE,
                UIText.WEEKLY_SETTINGS_TITLE,
                question,
                positive,
                negative,
                votes,
                timeStr,
                dayShort,
                status);
        }

        public static InlineKeyboardMarkup getKeyboard(boolean enabled) {
            List<InlineKeyboardRow> rows = new ArrayList<>();
            rows.add(new InlineKeyboardRow(List.of(InlineKeyboardButton.builder()
                .text(UIText.BUTTON_EDIT_QUESTION)
                .callbackData("weekly:config:question")
                .build())));
            rows.add(new InlineKeyboardRow(List.of(
                InlineKeyboardButton.builder()
                    .text(UIText.BUTTON_EDIT_POSITIVE)
                    .callbackData("weekly:config:positive")
                    .build(),
                InlineKeyboardButton.builder()
                    .text(UIText.BUTTON_EDIT_NEGATIVE)
                    .callbackData("weekly:config:negative")
                    .build())));
            rows.add(new InlineKeyboardRow(List.of(InlineKeyboardButton.builder()
                .text(UIText.BUTTON_EDIT_TARGET)
                .callbackData("weekly:config:votes")
                .build())));
            rows.add(new InlineKeyboardRow(List.of(
                InlineKeyboardButton.builder().text(UIText.BUTTON_WEEKLY_DAY).callbackData("weekly:config:day").build(),
                InlineKeyboardButton.builder()
                    .text(UIText.BUTTON_WEEKLY_TIME)
                    .callbackData("weekly:config:time")
                    .build())));
            rows.add(new InlineKeyboardRow(List.of(InlineKeyboardButton.builder()
                .text(enabled ? UIText.BUTTON_TOGGLE_DISABLE : UIText.BUTTON_TOGGLE_ENABLE)
                .callbackData("weekly:config:toggle")
                .build())));
            rows.add(new InlineKeyboardRow(
                List.of(InlineKeyboardButton.builder().text(UIText.BUTTON_BACK).callbackData("main:menu").build())));
            return InlineKeyboardMarkup.builder().keyboard(rows).build();
        }
    }

    public static class WeeklyDayPage {
        public static String getText() {
            return UIText.WEEKLY_DAY_TITLE;
        }

        public static InlineKeyboardMarkup getKeyboard() {
            List<InlineKeyboardRow> rows = new ArrayList<>();
            String[] days = { UIText.DAY_MONDAY, UIText.DAY_TUESDAY, UIText.DAY_WEDNESDAY, UIText.DAY_THURSDAY,
                    UIText.DAY_FRIDAY, UIText.DAY_SATURDAY, UIText.DAY_SUNDAY };
            for (int i = 0; i < 7; i++) {
                String d = days[i];
                String cb = "weekly:day:" + (i + 1);
                rows.add(
                    new InlineKeyboardRow(List.of(InlineKeyboardButton.builder().text(d).callbackData(cb).build())));
            }
            rows.add(new InlineKeyboardRow(
                List.of(InlineKeyboardButton.builder().text(UIText.BUTTON_BACK).callbackData("weekly:menu").build())));
            return InlineKeyboardMarkup.builder().keyboard(rows).build();
        }
    }

    public static class WeeklyTimePage {
        public static String getText() {
            return UIText.WEEKLY_TIME_TITLE;
        }

        public static InlineKeyboardMarkup getKeyboard() {
            List<InlineKeyboardRow> rows = new ArrayList<>();
            rows.add(new InlineKeyboardRow(
                List.of(InlineKeyboardButton.builder().text(UIText.BUTTON_BACK).callbackData("weekly:menu").build())));
            return InlineKeyboardMarkup.builder().keyboard(rows).build();
        }
    }
}
