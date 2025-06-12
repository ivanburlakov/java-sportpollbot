package io.sportpoll.bot.unit.ui;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import io.sportpoll.bot.ui.Pages;
import io.sportpoll.bot.constants.UIText;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PagesTest {

    @Test
    void testEditPageGetText() {
        String result = Pages.EditPage.getText("Test Title", "Current Value");
        assertTrue(result.contains("Test Title"));
        assertTrue(result.contains("Current Value"));
        assertTrue(result.contains(UIText.EDIT_CURRENT_VALUE.split(" ")[0])); // "Поточне"
        assertTrue(result.contains(UIText.EDIT_ENTER_NEW_VALUE));
    }

    @Test
    void testEditPageGetKeyboard() {
        InlineKeyboardMarkup keyboard = Pages.EditPage.getKeyboard("back:target");
        assertNotNull(keyboard);
        assertEquals(1, keyboard.getKeyboard().size());
        assertEquals(1, keyboard.getKeyboard().get(0).size());
        InlineKeyboardButton button = keyboard.getKeyboard().get(0).get(0);
        assertEquals(UIText.BUTTON_CANCEL, button.getText());
        assertEquals("back:target", button.getCallbackData());
    }

    @Test
    void testMainMenuPageGetText() {
        String resultWithPoll = Pages.MainMenuPage.getText(true);
        String resultWithoutPoll = Pages.MainMenuPage.getText(false);
        assertTrue(resultWithPoll.contains("Адміністрування SportPoll Bot"));
        assertTrue(resultWithoutPoll.contains("Адміністрування SportPoll Bot"));
        assertTrue(resultWithPoll.contains("Виберіть опцію"));
        assertTrue(resultWithoutPoll.contains("Виберіть опцію"));
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
        assertEquals(UIText.BUTTON_CREATE_POLL, createButton.getText());
        assertEquals("main:create", createButton.getCallbackData());

        InlineKeyboardButton closeButton = keyboardWithPoll.getKeyboard().get(0).get(1);
        assertEquals(UIText.BUTTON_CLOSE_POLL, closeButton.getText());
        assertEquals("main:close", closeButton.getCallbackData());

        InlineKeyboardButton weeklyButton = keyboardWithPoll.getKeyboard().get(1).get(0);
        assertEquals(UIText.BUTTON_WEEKLY_SETTINGS, weeklyButton.getText());
        assertEquals("main:weekly", weeklyButton.getCallbackData());
    }

    @Test
    void testCreatePollPageGetText() {
        String result = Pages.CreatePollPage.getText("Test Question?", "Yes Option", "No Option", 10);

        assertTrue(result.contains(UIText.TEXT_CREATE_POLL));
        assertTrue(result.contains("Test Question?"));
        assertTrue(result.contains("Yes Option"));
        assertTrue(result.contains("No Option"));
        assertTrue(result.contains("10 " + UIText.TEXT_VOTES));
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
        assertEquals(UIText.BUTTON_EDIT_QUESTION, questionButton.getText());
        assertEquals("poll:edit:question", questionButton.getCallbackData());

        InlineKeyboardButton positiveButton = keyboard.getKeyboard().get(1).get(0);
        assertEquals(UIText.BUTTON_EDIT_POSITIVE, positiveButton.getText());
        assertEquals("poll:edit:positive", positiveButton.getCallbackData());

        InlineKeyboardButton negativeButton = keyboard.getKeyboard().get(1).get(1);
        assertEquals(UIText.BUTTON_EDIT_NEGATIVE, negativeButton.getText());
        assertEquals("poll:edit:negative", negativeButton.getCallbackData());

        InlineKeyboardButton votesButton = keyboard.getKeyboard().get(2).get(0);
        assertEquals(UIText.BUTTON_EDIT_TARGET, votesButton.getText());
        assertEquals("poll:edit:votes", votesButton.getCallbackData());

        InlineKeyboardButton confirmButton = keyboard.getKeyboard().get(3).get(0);
        assertEquals(UIText.BUTTON_CREATE, confirmButton.getText());
        assertEquals("poll:confirm", confirmButton.getCallbackData());

        InlineKeyboardButton cancelButton = keyboard.getKeyboard().get(3).get(1);
        assertEquals(UIText.BUTTON_CANCEL, cancelButton.getText());
        assertEquals("main:menu", cancelButton.getCallbackData());
    }

    @Test
    void testWeeklySettingsPageGetText() {
        String result = Pages.WeeklySettingsPage
            .getText("Weekly Question?", "Yes", "No", 5, "14:30:00", "FRIDAY", true);

        assertTrue(result.contains(UIText.TEXT_WEEKLY_SETTINGS));
        assertTrue(result.contains("Weekly Question?"));
        assertTrue(result.contains("Yes"));
        assertTrue(result.contains("No"));
        assertTrue(result.contains("5 " + UIText.TEXT_VOTES));
        assertTrue(result.contains(UIText.WEEKLY_TIME_PREFIX + "14:30"));
        assertTrue(result.contains(UIText.DAY_FRIDAY));
        assertTrue(result.contains(UIText.WEEKLY_STATUS_ENABLED));
    }

    @Test
    void testWeeklySettingsPageGetTextWithDifferentDays() {
        String mondayResult = Pages.WeeklySettingsPage.getText("Q?", "Y", "N", 1, "10:00:00", "MONDAY", false);
        assertTrue(mondayResult.contains(UIText.DAY_MONDAY));
        assertTrue(mondayResult.contains(UIText.WEEKLY_STATUS_DISABLED));

        String tuesdayResult = Pages.WeeklySettingsPage.getText("Q?", "Y", "N", 1, "10:00:00", "TUESDAY", true);
        assertTrue(tuesdayResult.contains(UIText.DAY_TUESDAY));

        String wednesdayResult = Pages.WeeklySettingsPage.getText("Q?", "Y", "N", 1, "10:00:00", "WEDNESDAY", true);
        assertTrue(wednesdayResult.contains(UIText.DAY_WEDNESDAY));

        String thursdayResult = Pages.WeeklySettingsPage.getText("Q?", "Y", "N", 1, "10:00:00", "THURSDAY", true);
        assertTrue(thursdayResult.contains(UIText.DAY_THURSDAY));

        String saturdayResult = Pages.WeeklySettingsPage.getText("Q?", "Y", "N", 1, "10:00:00", "SATURDAY", true);
        assertTrue(saturdayResult.contains(UIText.DAY_SATURDAY));

        String sundayResult = Pages.WeeklySettingsPage.getText("Q?", "Y", "N", 1, "10:00:00", "SUNDAY", true);
        assertTrue(sundayResult.contains(UIText.DAY_SUNDAY));
    }

    @Test
    void testWeeklySettingsPageGetKeyboardEnabled() {
        InlineKeyboardMarkup keyboard = Pages.WeeklySettingsPage.getKeyboard(true);

        assertNotNull(keyboard);
        assertEquals(6, keyboard.getKeyboard().size());

        InlineKeyboardButton toggleButton = keyboard.getKeyboard().get(4).get(0);
        assertEquals(UIText.BUTTON_TOGGLE_DISABLE, toggleButton.getText());
        assertEquals("weekly:config:toggle", toggleButton.getCallbackData());
    }

    @Test
    void testWeeklySettingsPageGetKeyboardDisabled() {
        InlineKeyboardMarkup keyboard = Pages.WeeklySettingsPage.getKeyboard(false);

        assertNotNull(keyboard);
        assertEquals(6, keyboard.getKeyboard().size());

        InlineKeyboardButton toggleButton = keyboard.getKeyboard().get(4).get(0);
        assertEquals(UIText.BUTTON_TOGGLE_ENABLE, toggleButton.getText());
        assertEquals("weekly:config:toggle", toggleButton.getCallbackData());
    }

    @Test
    void testWeeklyDayPageGetText() {
        String result = Pages.WeeklyDayPage.getText();
        assertTrue(result.contains(UIText.TEXT_SELECT_DAY));
    }

    @Test
    void testWeeklyDayPageGetKeyboard() {
        InlineKeyboardMarkup keyboard = Pages.WeeklyDayPage.getKeyboard();

        assertNotNull(keyboard);
        assertEquals(8, keyboard.getKeyboard().size()); // 7 days + back button

        String[] expectedDays = { UIText.DAY_MONDAY, UIText.DAY_TUESDAY, UIText.DAY_WEDNESDAY, UIText.DAY_THURSDAY,
                UIText.DAY_FRIDAY, UIText.DAY_SATURDAY, UIText.DAY_SUNDAY };
        for (int i = 0; i < 7; i++) {
            InlineKeyboardButton dayButton = keyboard.getKeyboard().get(i).get(0);
            assertEquals(expectedDays[i], dayButton.getText());
            assertEquals("weekly:day:" + (i + 1), dayButton.getCallbackData());
        }

        InlineKeyboardButton backButton = keyboard.getKeyboard().get(7).get(0);
        assertEquals(UIText.BUTTON_BACK, backButton.getText());
        assertEquals("weekly:menu", backButton.getCallbackData());
    }

    @Test
    void testWeeklyTimePageGetText() {
        String result = Pages.WeeklyTimePage.getText();
        assertTrue(result.contains(UIText.TEXT_ENTER_HOUR));
        assertTrue(result.contains(UIText.TEXT_0_TO_23));
        assertTrue(result.contains(UIText.TEXT_24_HOUR_FORMAT));
    }

    @Test
    void testWeeklyTimePageGetKeyboard() {
        InlineKeyboardMarkup keyboard = Pages.WeeklyTimePage.getKeyboard();

        assertNotNull(keyboard);
        assertEquals(1, keyboard.getKeyboard().size());

        InlineKeyboardButton backButton = keyboard.getKeyboard().get(0).get(0);
        assertEquals(UIText.BUTTON_BACK, backButton.getText());
        assertEquals("weekly:menu", backButton.getCallbackData());
    }
}
