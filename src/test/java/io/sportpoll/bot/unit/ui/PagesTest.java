package io.sportpoll.bot.unit.ui;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import io.sportpoll.bot.ui.Pages;

import static org.junit.jupiter.api.Assertions.*;

class PagesTest {

    @Test
    void testEditPageGetText() {
        // Generate edit page text with title and current value
        String result = Pages.EditPage.getText("Test Title", "Current Value");
        // Verify text contains all expected elements
        assertTrue(result.contains("Test Title"));
        assertTrue(result.contains("Current Value"));
        assertTrue(result.contains("Поточне значення"));
        assertTrue(result.contains("Введіть нове значення"));
    }

    @Test
    void testEditPageGetKeyboard() {
        // Generate edit page keyboard with back target
        InlineKeyboardMarkup keyboard = Pages.EditPage.getKeyboard("back:target");
        // Verify keyboard structure
        assertNotNull(keyboard);
        assertEquals(1, keyboard.getKeyboard().size());
        assertEquals(1, keyboard.getKeyboard().get(0).size());
        // Verify cancel button properties
        InlineKeyboardButton button = keyboard.getKeyboard().get(0).get(0);
        assertEquals("❌ Скасувати", button.getText());
        assertEquals("back:target", button.getCallbackData());
    }

    @Test
    void testMainMenuPageGetText() {
        // Generate main menu text for both poll states
        String resultWithPoll = Pages.MainMenuPage.getText(true);
        String resultWithoutPoll = Pages.MainMenuPage.getText(false);
        // Verify text contains expected content
        assertTrue(resultWithPoll.contains("Адміністрування SportPoll Bot"));
        assertTrue(resultWithoutPoll.contains("Адміністрування SportPoll Bot"));
        assertTrue(resultWithPoll.contains("Виберіть опцію"));
        assertTrue(resultWithoutPoll.contains("Виберіть опцію"));
        // Verify text is same regardless of poll state
        assertEquals(resultWithPoll, resultWithoutPoll);
    }

    @Test
    void testMainMenuPageGetKeyboard() {
        // Generate keyboards for both poll states
        InlineKeyboardMarkup keyboardWithPoll = Pages.MainMenuPage.getKeyboard(true);
        InlineKeyboardMarkup keyboardWithoutPoll = Pages.MainMenuPage.getKeyboard(false);

        assertNotNull(keyboardWithPoll);
        assertNotNull(keyboardWithoutPoll);
        assertEquals(2, keyboardWithPoll.getKeyboard().size());
        assertEquals(2, keyboardWithoutPoll.getKeyboard().size());

        assertEquals(2, keyboardWithPoll.getKeyboard().get(0).size());
        assertEquals(1, keyboardWithPoll.getKeyboard().get(1).size());

        InlineKeyboardButton createButton = keyboardWithPoll.getKeyboard().get(0).get(0);
        assertEquals("📊 Створити опитування", createButton.getText());
        assertEquals("main:create", createButton.getCallbackData());

        InlineKeyboardButton closeButton = keyboardWithPoll.getKeyboard().get(0).get(1);
        assertEquals("❌ Закрити опитування", closeButton.getText());
        assertEquals("main:close", closeButton.getCallbackData());

        InlineKeyboardButton weeklyButton = keyboardWithPoll.getKeyboard().get(1).get(0);
        assertEquals("⚙️ Тижневі налаштування", weeklyButton.getText());
        assertEquals("main:weekly", weeklyButton.getCallbackData());
    }

    @Test
    void testCreatePollPageGetText() {
        String result = Pages.CreatePollPage.getText("Test Question?", "Yes Option", "No Option", 10);

        assertTrue(result.contains("Створення опитування"));
        assertTrue(result.contains("Test Question?"));
        assertTrue(result.contains("Yes Option"));
        assertTrue(result.contains("No Option"));
        assertTrue(result.contains("10 голосів"));
    }

    @Test
    void testCreatePollPageGetKeyboard() {
        InlineKeyboardMarkup keyboard = Pages.CreatePollPage.getKeyboard();

        assertNotNull(keyboard);
        assertEquals(4, keyboard.getKeyboard().size());

        assertEquals(1, keyboard.getKeyboard().get(0).size());
        assertEquals(2, keyboard.getKeyboard().get(1).size());
        assertEquals(1, keyboard.getKeyboard().get(2).size());
        assertEquals(2, keyboard.getKeyboard().get(3).size());

        InlineKeyboardButton questionButton = keyboard.getKeyboard().get(0).get(0);
        assertEquals("📝 Змінити питання", questionButton.getText());
        assertEquals("poll:edit:question", questionButton.getCallbackData());

        InlineKeyboardButton positiveButton = keyboard.getKeyboard().get(1).get(0);
        assertEquals("✅ Змінити \"за\"", positiveButton.getText());
        assertEquals("poll:edit:positive", positiveButton.getCallbackData());

        InlineKeyboardButton negativeButton = keyboard.getKeyboard().get(1).get(1);
        assertEquals("❌ Змінити \"проти\"", negativeButton.getText());
        assertEquals("poll:edit:negative", negativeButton.getCallbackData());

        InlineKeyboardButton votesButton = keyboard.getKeyboard().get(2).get(0);
        assertEquals("🎯 Змінити ціль", votesButton.getText());
        assertEquals("poll:edit:votes", votesButton.getCallbackData());

        InlineKeyboardButton confirmButton = keyboard.getKeyboard().get(3).get(0);
        assertEquals("✅ Створити", confirmButton.getText());
        assertEquals("poll:confirm", confirmButton.getCallbackData());

        InlineKeyboardButton cancelButton = keyboard.getKeyboard().get(3).get(1);
        assertEquals("❌ Скасувати", cancelButton.getText());
        assertEquals("main:menu", cancelButton.getCallbackData());
    }

    @Test
    void testWeeklySettingsPageGetText() {
        String result = Pages.WeeklySettingsPage
            .getText("Weekly Question?", "Yes", "No", 5, "14:30:00", "FRIDAY", true);

        assertTrue(result.contains("Тижневі налаштування"));
        assertTrue(result.contains("Weekly Question?"));
        assertTrue(result.contains("Yes"));
        assertTrue(result.contains("No"));
        assertTrue(result.contains("5 голосів"));
        assertTrue(result.contains("з 14:30"));
        assertTrue(result.contains("Пт"));
        assertTrue(result.contains("Увімкнено"));
    }

    @Test
    void testWeeklySettingsPageGetTextWithDifferentDays() {
        String mondayResult = Pages.WeeklySettingsPage.getText("Q?", "Y", "N", 1, "10:00:00", "MONDAY", false);
        assertTrue(mondayResult.contains("Пн"));
        assertTrue(mondayResult.contains("Вимкнено"));

        String tuesdayResult = Pages.WeeklySettingsPage.getText("Q?", "Y", "N", 1, "10:00:00", "TUESDAY", true);
        assertTrue(tuesdayResult.contains("Вт"));

        String wednesdayResult = Pages.WeeklySettingsPage.getText("Q?", "Y", "N", 1, "10:00:00", "WEDNESDAY", true);
        assertTrue(wednesdayResult.contains("Ср"));

        String thursdayResult = Pages.WeeklySettingsPage.getText("Q?", "Y", "N", 1, "10:00:00", "THURSDAY", true);
        assertTrue(thursdayResult.contains("Чт"));

        String saturdayResult = Pages.WeeklySettingsPage.getText("Q?", "Y", "N", 1, "10:00:00", "SATURDAY", true);
        assertTrue(saturdayResult.contains("Сб"));

        String sundayResult = Pages.WeeklySettingsPage.getText("Q?", "Y", "N", 1, "10:00:00", "SUNDAY", true);
        assertTrue(sundayResult.contains("Нд"));

        String unknownDayResult = Pages.WeeklySettingsPage.getText("Q?", "Y", "N", 1, "10:00:00", "UNKNOWN", true);
        assertTrue(unknownDayResult.contains("UNKNOWN"));
    }

    @Test
    void testWeeklySettingsPageGetKeyboardEnabled() {
        InlineKeyboardMarkup keyboard = Pages.WeeklySettingsPage.getKeyboard(true);

        assertNotNull(keyboard);
        assertEquals(6, keyboard.getKeyboard().size());

        InlineKeyboardButton toggleButton = keyboard.getKeyboard().get(4).get(0);
        assertEquals("🔴 Вимкнути", toggleButton.getText());
        assertEquals("weekly:config:toggle", toggleButton.getCallbackData());
    }

    @Test
    void testWeeklySettingsPageGetKeyboardDisabled() {
        InlineKeyboardMarkup keyboard = Pages.WeeklySettingsPage.getKeyboard(false);

        assertNotNull(keyboard);
        assertEquals(6, keyboard.getKeyboard().size());

        InlineKeyboardButton toggleButton = keyboard.getKeyboard().get(4).get(0);
        assertEquals("🟢 Увімкнути", toggleButton.getText());
        assertEquals("weekly:config:toggle", toggleButton.getCallbackData());
    }

    @Test
    void testWeeklyDayPageGetText() {
        String result = Pages.WeeklyDayPage.getText();
        assertTrue(result.contains("Оберіть день тижня"));
    }

    @Test
    void testWeeklyDayPageGetKeyboard() {
        InlineKeyboardMarkup keyboard = Pages.WeeklyDayPage.getKeyboard();

        assertNotNull(keyboard);
        assertEquals(8, keyboard.getKeyboard().size()); // 7 days + back button

        String[] expectedDays = { "Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд" };
        for (int i = 0; i < 7; i++) {
            InlineKeyboardButton dayButton = keyboard.getKeyboard().get(i).get(0);
            assertEquals(expectedDays[i], dayButton.getText());
            assertEquals("weekly:day:" + (i + 1), dayButton.getCallbackData());
        }

        InlineKeyboardButton backButton = keyboard.getKeyboard().get(7).get(0);
        assertEquals("◀️ Назад", backButton.getText());
        assertEquals("weekly:menu", backButton.getCallbackData());
    }

    @Test
    void testWeeklyTimePageGetText() {
        String result = Pages.WeeklyTimePage.getText();
        assertTrue(result.contains("Введіть годину початку"));
        assertTrue(result.contains("0-23"));
        assertTrue(result.contains("24-годинному форматі"));
    }

    @Test
    void testWeeklyTimePageGetKeyboard() {
        InlineKeyboardMarkup keyboard = Pages.WeeklyTimePage.getKeyboard();

        assertNotNull(keyboard);
        assertEquals(1, keyboard.getKeyboard().size());

        InlineKeyboardButton backButton = keyboard.getKeyboard().get(0).get(0);
        assertEquals("◀️ Назад", backButton.getText());
        assertEquals("weekly:menu", backButton.getCallbackData());
    }
}
