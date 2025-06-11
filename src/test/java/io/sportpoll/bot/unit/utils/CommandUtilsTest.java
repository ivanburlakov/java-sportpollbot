package io.sportpoll.bot.unit.utils;

import io.sportpoll.bot.utils.CommandUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CommandUtilsTest {

    @Test
    void testIsCommand() {
        // Test valid command formats
        assertTrue(CommandUtils.isCommand("/start"));
        assertTrue(CommandUtils.isCommand("/help argument"));
        assertTrue(CommandUtils.isCommand("  /test  "));
        // Test invalid command formats
        assertFalse(CommandUtils.isCommand("hello"));
        assertFalse(CommandUtils.isCommand(""));
        assertFalse(CommandUtils.isCommand(null));
        assertFalse(CommandUtils.isCommand("   "));
        assertFalse(CommandUtils.isCommand("hello /test"));
    }

    @Test
    void testParseCommand() {
        // Test command extraction from various inputs
        assertEquals("/start", CommandUtils.parseCommand("/start"));
        assertEquals("/help", CommandUtils.parseCommand("/help argument"));
        assertEquals("/test", CommandUtils.parseCommand("  /test  "));
        assertEquals("/cmd", CommandUtils.parseCommand("/cmd arg1 arg2"));
        // Test non-command inputs return empty string
        assertEquals("", CommandUtils.parseCommand("hello"));
        assertEquals("", CommandUtils.parseCommand(""));
        assertEquals("", CommandUtils.parseCommand(null));
        assertEquals("", CommandUtils.parseCommand("   "));
    }

    @Test
    void testParseArguments() {
        // Test argument parsing from command strings
        assertArrayEquals(new String[0], CommandUtils.parseArguments("/start"));
        assertArrayEquals(new String[] { "arg1" }, CommandUtils.parseArguments("/help arg1"));
        assertArrayEquals(new String[] { "arg1", "arg2" }, CommandUtils.parseArguments("/cmd arg1 arg2"));
        assertArrayEquals(new String[] { "John", "Jane", "Bob" }, CommandUtils.parseArguments("/+ John Jane Bob"));
        // Test non-command inputs return empty array
        assertArrayEquals(new String[0], CommandUtils.parseArguments("hello"));
        assertArrayEquals(new String[0], CommandUtils.parseArguments(""));
        assertArrayEquals(new String[0], CommandUtils.parseArguments(null));
        assertArrayEquals(new String[0], CommandUtils.parseArguments("   "));
    }

    @Test
    void testCommandNormalizationForVoting() {
        assertEquals("/+", CommandUtils.parseCommand("/+"));
        assertEquals("/+", CommandUtils.parseCommand("/+2"));
        assertEquals("/+", CommandUtils.parseCommand("/+Alice"));
        assertEquals("/+", CommandUtils.parseCommand("/+ 2"));
        assertEquals("/+", CommandUtils.parseCommand("/+ Alice"));

        assertArrayEquals(new String[] { "2" }, CommandUtils.parseArguments("/+2"));
        assertArrayEquals(new String[] { "2" }, CommandUtils.parseArguments("/+ 2"));
        assertArrayEquals(new String[] { "5" }, CommandUtils.parseArguments("/+5"));
        assertArrayEquals(new String[] { "Alice" }, CommandUtils.parseArguments("/+Alice"));
        assertArrayEquals(new String[] { "Alice", "Bob" }, CommandUtils.parseArguments("/+Alice Bob"));
        assertArrayEquals(new String[] { "Alice" }, CommandUtils.parseArguments("/+ Alice"));
        assertArrayEquals(new String[] { "Alice", "Bob" }, CommandUtils.parseArguments("/+ Alice Bob"));
    }

    @Test
    void testCommandNormalizationForRevocation() {
        assertEquals("/-", CommandUtils.parseCommand("/-"));
        assertEquals("/-", CommandUtils.parseCommand("/-2"));
        assertEquals("/-", CommandUtils.parseCommand("/- 2"));

        assertArrayEquals(new String[] { "2" }, CommandUtils.parseArguments("/-2"));
        assertArrayEquals(new String[] { "2" }, CommandUtils.parseArguments("/- 2"));
        assertArrayEquals(new String[] { "5" }, CommandUtils.parseArguments("/-5"));
    }

    @Test
    void testComplexCommandFormats() {
        assertArrayEquals(new String[] { "John", "Jane", "Bob" }, CommandUtils.parseArguments("/+John Jane Bob"));
        assertArrayEquals(new String[] { "3" }, CommandUtils.parseArguments("/+3"));
        assertArrayEquals(new String[] { "10" }, CommandUtils.parseArguments("/+10"));
        assertArrayEquals(new String[] { "1" }, CommandUtils.parseArguments("/-1"));
        assertArrayEquals(new String[] { "7" }, CommandUtils.parseArguments("/-7"));
    }
}
