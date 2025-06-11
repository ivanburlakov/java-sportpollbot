package io.sportpoll.bot.ui;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import java.util.ArrayList;
import java.util.List;

public class Pages {
    public static class EditPage {
        public static String getText(String title, String currentValue) {
            return String.format("""
                %s

                Поточне значення: %s

                Введіть нове значення:""", title, currentValue);
        }

        public static InlineKeyboardMarkup getKeyboard(String backTarget) {
            List<InlineKeyboardRow> rows = new ArrayList<>();
            InlineKeyboardRow row = new InlineKeyboardRow();
            row.add(InlineKeyboardButton.builder().text("❌ Скасувати").callbackData(backTarget).build());
            rows.add(row);
            return InlineKeyboardMarkup.builder().keyboard(rows).build();
        }
    }

    public static class MainMenuPage {
        public static String getText(boolean hasActivePoll) {
            return "🤖 <b>Адміністрування SportPoll Bot</b>\n\nВиберіть опцію:";
        }

        public static InlineKeyboardMarkup getKeyboard(boolean hasActivePoll) {
            List<InlineKeyboardRow> rows = new ArrayList<>();
            InlineKeyboardRow row1 = new InlineKeyboardRow();
            row1.add(InlineKeyboardButton.builder().text("📊 Створити опитування").callbackData("main:create").build());
            row1.add(InlineKeyboardButton.builder().text("❌ Закрити опитування").callbackData("main:close").build());
            rows.add(row1);
            InlineKeyboardRow row2 = new InlineKeyboardRow();
            row2.add(
                InlineKeyboardButton.builder().text("⚙️ Тижневі налаштування").callbackData("main:weekly").build());
            rows.add(row2);
            return InlineKeyboardMarkup.builder().keyboard(rows).build();
        }
    }

    public static class CreatePollPage {
        public static String getText(String question, String positive, String negative, int votes) {
            return String.format("""
                📊 <b>Створення опитування</b>

                📝 Питання: %s
                ✅ За: %s
                ❌ Проти: %s
                🎯 Ціль: %d голосів""", question, positive, negative, votes);
        }

        public static InlineKeyboardMarkup getKeyboard() {
            List<InlineKeyboardRow> rows = new ArrayList<>();

            InlineKeyboardRow row1 = new InlineKeyboardRow();
            row1.add(
                InlineKeyboardButton.builder().text("📝 Змінити питання").callbackData("poll:edit:question").build());
            rows.add(row1);

            InlineKeyboardRow row2 = new InlineKeyboardRow();
            row2.add(
                InlineKeyboardButton.builder().text("✅ Змінити \"за\"").callbackData("poll:edit:positive").build());
            row2.add(
                InlineKeyboardButton.builder().text("❌ Змінити \"проти\"").callbackData("poll:edit:negative").build());
            rows.add(row2);

            InlineKeyboardRow row3 = new InlineKeyboardRow();
            row3.add(InlineKeyboardButton.builder().text("🎯 Змінити ціль").callbackData("poll:edit:votes").build());
            rows.add(row3);

            InlineKeyboardRow row4 = new InlineKeyboardRow();
            row4.add(InlineKeyboardButton.builder().text("✅ Створити").callbackData("poll:confirm").build());
            row4.add(InlineKeyboardButton.builder().text("❌ Скасувати").callbackData("main:menu").build());
            rows.add(row4);

            return InlineKeyboardMarkup.builder().keyboard(rows).build();
        }
    }

    public static class WeeklySettingsPage {
        public static String getText(String question, String positive, String negative, int votes, String time,
            String day, boolean enabled) {
            String dayShort = switch (day) {
                case "MONDAY" -> "Пн";
                case "TUESDAY" -> "Вт";
                case "WEDNESDAY" -> "Ср";
                case "THURSDAY" -> "Чт";
                case "FRIDAY" -> "Пт";
                case "SATURDAY" -> "Сб";
                case "SUNDAY" -> "Нд";
                default -> day;
            };
            String timeStr = "з " + time.substring(0, 5);
            return String.format("""
                ⚙️ <b>Тижневі налаштування</b>

                📝 Питання: %s
                ✅ За: %s
                ❌ Проти: %s
                🎯 Ціль: %d голосів
                ⏰ Час: %s
                📅 День: %s
                🔄 Статус: %s""",
                question,
                positive,
                negative,
                votes,
                timeStr,
                dayShort,
                enabled ? "Увімкнено" : "Вимкнено");
        }

        public static InlineKeyboardMarkup getKeyboard(boolean enabled) {
            List<InlineKeyboardRow> rows = new ArrayList<>();
            rows.add(new InlineKeyboardRow(List.of(InlineKeyboardButton.builder()
                .text("📝 Змінити питання")
                .callbackData("weekly:config:question")
                .build())));
            rows.add(new InlineKeyboardRow(List.of(
                InlineKeyboardButton.builder().text("✅ Змінити \"за\"").callbackData("weekly:config:positive").build(),
                InlineKeyboardButton.builder()
                    .text("❌ Змінити \"проти\"")
                    .callbackData("weekly:config:negative")
                    .build())));
            rows.add(new InlineKeyboardRow(List.of(
                InlineKeyboardButton.builder().text("🎯 Змінити ціль").callbackData("weekly:config:votes").build())));
            rows.add(new InlineKeyboardRow(
                List.of(InlineKeyboardButton.builder().text("📅 День").callbackData("weekly:config:day").build(),
                    InlineKeyboardButton.builder().text("⏰ Час").callbackData("weekly:config:time").build())));
            rows.add(new InlineKeyboardRow(List.of(InlineKeyboardButton.builder()
                .text(enabled ? "🔴 Вимкнути" : "🟢 Увімкнути")
                .callbackData("weekly:config:toggle")
                .build())));
            rows.add(new InlineKeyboardRow(
                List.of(InlineKeyboardButton.builder().text("◀️ Назад").callbackData("main:menu").build())));
            return InlineKeyboardMarkup.builder().keyboard(rows).build();
        }
    }

    public static class WeeklyDayPage {
        public static String getText() {
            return "Оберіть день тижня для опитування:";
        }

        public static InlineKeyboardMarkup getKeyboard() {
            List<InlineKeyboardRow> rows = new ArrayList<>();
            String[] days = { "Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд" };
            for (int i = 0; i < 7; i++) {
                String d = days[i];
                String cb = "weekly:day:" + (i + 1);
                rows.add(
                    new InlineKeyboardRow(List.of(InlineKeyboardButton.builder().text(d).callbackData(cb).build())));
            }
            rows.add(new InlineKeyboardRow(
                List.of(InlineKeyboardButton.builder().text("◀️ Назад").callbackData("weekly:menu").build())));
            return InlineKeyboardMarkup.builder().keyboard(rows).build();
        }
    }

    public static class WeeklyTimePage {
        public static String getText() {
            return "Введіть годину початку опитування (0-23, у 24-годинному форматі, наприклад: 13):";
        }

        public static InlineKeyboardMarkup getKeyboard() {
            List<InlineKeyboardRow> rows = new ArrayList<>();
            rows.add(new InlineKeyboardRow(
                List.of(InlineKeyboardButton.builder().text("◀️ Назад").callbackData("weekly:menu").build())));
            return InlineKeyboardMarkup.builder().keyboard(rows).build();
        }
    }
}
