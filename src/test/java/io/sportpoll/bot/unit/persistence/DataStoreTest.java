package io.sportpoll.bot.unit.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import io.sportpoll.bot.config.Config;
import io.sportpoll.bot.persistance.DataStore;
import io.sportpoll.bot.unit.utils.TestUtils;
import java.io.File;
import java.io.Serializable;
import static org.junit.jupiter.api.Assertions.*;

public class DataStoreTest {

    @TempDir
    File tempDir;

    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        Config.setInstance(TestUtils.createTestConfig());
        dataStore = new DataStore(tempDir);
    }

    @AfterEach
    void tearDown() {
        Config.setInstance(null);
    }

    @Test
    void testGetSingleton() {
        // Register test class for singleton management
        dataStore.autoRegister(TestSerializableClass.class);
        // Get instance twice to test singleton behavior
        TestSerializableClass instance1 = dataStore.get(TestSerializableClass.class);
        TestSerializableClass instance2 = dataStore.get(TestSerializableClass.class);
        // Verify both calls return same instance
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    void testSaveSingleton() {
        // Register and get singleton instance
        dataStore.autoRegister(TestSerializableClass.class);
        TestSerializableClass instance = dataStore.get(TestSerializableClass.class);
        // Verify save operation completes without errors
        assertDoesNotThrow(() -> dataStore.save(instance));
    }

    @Test
    void testAutoRegister() {
        // Register test class as singleton
        assertDoesNotThrow(() -> dataStore.autoRegister(TestSerializableClass.class));
        // Get registered instance
        TestSerializableClass instance = dataStore.get(TestSerializableClass.class);
        // Verify instance exists with correct default value
        assertNotNull(instance);
        assertEquals("default", instance.getValue());
    }

    private static class TestSerializableClass implements Serializable {
        private final String value = "default";

        public String getValue() {
            return value;
        }
    }
}
