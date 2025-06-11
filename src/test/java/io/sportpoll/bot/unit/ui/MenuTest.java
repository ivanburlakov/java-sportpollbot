package io.sportpoll.bot.unit.ui;

import org.junit.jupiter.api.Test;

import io.sportpoll.bot.ui.Menu;

import static org.junit.jupiter.api.Assertions.*;

class MenuTest {

    @Test
    void testMenuValues() {
        // Get all menu enum values
        Menu[] menus = Menu.values();
        // Verify correct number of menu items
        assertEquals(9, menus.length);
        // Verify specific menu ordering
        assertEquals(Menu.MAIN_MENU, menus[0]);
        assertEquals(Menu.CREATE_POLL, menus[1]);
        assertEquals(Menu.WEEKLY_SETTINGS, menus[2]);
        assertEquals(Menu.WEEKLY_CONFIG, menus[3]);
        assertEquals(Menu.CONFIRM_POLL, menus[4]);
        assertEquals(Menu.EDIT_POLL_QUESTION, menus[5]);
        assertEquals(Menu.EDIT_POLL_POSITIVE, menus[6]);
        assertEquals(Menu.EDIT_POLL_NEGATIVE, menus[7]);
        assertEquals(Menu.EDIT_POLL_VOTES, menus[8]);
    }

    @Test
    void testGetId() {
        // Test menu ID generation for each menu type
        assertEquals("main:menu", Menu.MAIN_MENU.getId());
        assertEquals("main:create", Menu.CREATE_POLL.getId());
        assertEquals("main:weekly", Menu.WEEKLY_SETTINGS.getId());
        assertEquals("weekly:config", Menu.WEEKLY_CONFIG.getId());
        assertEquals("poll:confirm", Menu.CONFIRM_POLL.getId());
        assertEquals("poll:edit:question", Menu.EDIT_POLL_QUESTION.getId());
        assertEquals("poll:edit:positive", Menu.EDIT_POLL_POSITIVE.getId());
        assertEquals("poll:edit:negative", Menu.EDIT_POLL_NEGATIVE.getId());
        assertEquals("poll:edit:votes", Menu.EDIT_POLL_VOTES.getId());
    }

    @Test
    void testFromIdValidCases() {
        // Test menu lookup by ID for all valid cases
        assertEquals(Menu.MAIN_MENU, Menu.fromId("main:menu"));
        assertEquals(Menu.CREATE_POLL, Menu.fromId("main:create"));
        assertEquals(Menu.WEEKLY_SETTINGS, Menu.fromId("main:weekly"));
        assertEquals(Menu.WEEKLY_CONFIG, Menu.fromId("weekly:config"));
        assertEquals(Menu.CONFIRM_POLL, Menu.fromId("poll:confirm"));
        assertEquals(Menu.EDIT_POLL_QUESTION, Menu.fromId("poll:edit:question"));
        assertEquals(Menu.EDIT_POLL_POSITIVE, Menu.fromId("poll:edit:positive"));
        assertEquals(Menu.EDIT_POLL_NEGATIVE, Menu.fromId("poll:edit:negative"));
        assertEquals(Menu.EDIT_POLL_VOTES, Menu.fromId("poll:edit:votes"));
    }

    @Test
    void testFromIdInvalidCases() {
        // Test menu lookup with invalid IDs returns null
        assertNull(Menu.fromId("invalid:id"));
        assertNull(Menu.fromId(""));
        assertNull(Menu.fromId(null));
        assertNull(Menu.fromId("main"));
        assertNull(Menu.fromId("MAIN_MENU"));
    }

    @Test
    void testFromIdCaseSensitive() {
        // Test that menu lookup is case-sensitive
        assertNull(Menu.fromId("Main:Menu"));
        assertNull(Menu.fromId("MAIN:MENU"));
        assertNull(Menu.fromId("main:MENU"));
    }

    @Test
    void testAllMenusHaveUniqueIds() {
        // Verify all menu IDs are unique
        Menu[] menus = Menu.values();
        for (int i = 0; i < menus.length; i++) {
            for (int j = i + 1; j < menus.length; j++) {
                assertNotEquals(menus[i].getId(),
                    menus[j].getId(),
                    "Menu " + menus[i] + " and " + menus[j] + " have the same ID");
            }
        }
    }

    @Test
    void testMenuIdsNotNull() {
        // Verify all menu IDs are non-null and non-empty
        for (Menu menu : Menu.values()) {
            assertNotNull(menu.getId(), "Menu " + menu + " has null ID");
            assertFalse(menu.getId().isEmpty(), "Menu " + menu + " has empty ID");
        }
    }
}
