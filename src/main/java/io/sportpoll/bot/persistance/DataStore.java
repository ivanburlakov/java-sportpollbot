package io.sportpoll.bot.persistance;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class DataStore {
    private static final DataStore instance = new DataStore();

    public static DataStore getInstance() {
        return instance;
    }

    static {
        instance.autoRegister(io.sportpoll.bot.services.PollManager.class);
        instance.autoRegister(io.sportpoll.bot.services.WeeklyPollScheduler.class);
        instance.autoRegister(io.sportpoll.bot.config.WeeklyPollConfig.class);
    }
    private final File dataDir;
    private final ConcurrentHashMap<Class<?>, Object> singletons = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, Supplier<?>> factories = new ConcurrentHashMap<>();

    public DataStore() {
        this.dataDir = new File("/tmp/sportpollbot");
        this.dataDir.mkdirs();
        preloadAll();
    }

    public DataStore(File customDataDir) {
        this.dataDir = customDataDir;
        this.dataDir.mkdirs();
    }

    public <T> void autoRegister(Class<T> type) {
        factories.put(type, () -> createInstance(type));
    }

    private <T> T createInstance(Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + type.getSimpleName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        return (T) singletons.computeIfAbsent(type, this::load);
    }

    private Object load(Class<?> type) {
        File file = new File(dataDir, type.getSimpleName() + ".dat");
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object obj = ois.readObject();
                return obj;
            } catch (Exception e) {
                System.err.println("Failed to load " + type.getSimpleName() + ": " + e.getMessage());
            }
        }
        Supplier<?> factory = factories.get(type);
        if (factory == null) {
            throw new IllegalStateException("No factory for " + type.getSimpleName());
        }
        Object instance = factory.get();
        save(instance);
        return instance;
    }

    public void save(Object obj) {
        try {
            File file = new File(dataDir, obj.getClass().getSimpleName() + ".dat");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(obj);
            }
        } catch (Exception e) {
            System.err.println("Failed to save " + obj.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    public void saveAll() {
        singletons.values().forEach(this::save);
    }

    private void preloadAll() {
        for (Class<?> c : factories.keySet()) {
            File file = new File(dataDir, c.getSimpleName() + ".dat");
            if (file.exists()) {
                System.out.println("Preloading " + c.getSimpleName() + "...");
                get(c);
            }
        }
    }

    public void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveAll));
    }
}
