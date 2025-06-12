package io.sportpoll.bot.constants;

public class UIText {

    // Edit Page
    public static final String EDIT_CURRENT_VALUE = "Поточне значення: %s";
    public static final String EDIT_ENTER_NEW_VALUE = "Введіть нове значення:";
    public static final String BUTTON_CANCEL = "❌ Скасувати";

    // Main Menu Page
    public static final String MAIN_MENU_TITLE = "🤖 <b>Адміністрування SportPoll Bot</b>\n\nВиберіть опцію:";
    public static final String BUTTON_CREATE_POLL = "📊 Створити опитування";
    public static final String BUTTON_CLOSE_POLL = "❌ Закрити опитування";
    public static final String BUTTON_WEEKLY_SETTINGS = "⚙️ Тижневі налаштування";

    // Create Poll Page
    public static final String CREATE_POLL_TITLE = "📊 <b>Створення опитування</b>";
    public static final String CREATE_POLL_TEMPLATE = """
        %s

        📝 Питання: %s
        ✅ За: %s
        ❌ Проти: %s
        🎯 Ціль: %d голосів""";
    public static final String BUTTON_EDIT_QUESTION = "📝 Змінити питання";
    public static final String BUTTON_EDIT_POSITIVE = "✅ Змінити \"за\"";
    public static final String BUTTON_EDIT_NEGATIVE = "❌ Змінити \"проти\"";
    public static final String BUTTON_EDIT_TARGET = "🎯 Змінити ціль";
    public static final String BUTTON_CREATE = "✅ Створити";

    // Weekly Settings Page
    public static final String WEEKLY_SETTINGS_TITLE = "⚙️ <b>Тижневі налаштування</b>";
    public static final String WEEKLY_SETTINGS_TEMPLATE = """
        %s

        📝 Питання: %s
        ✅ За: %s
        ❌ Проти: %s
        🎯 Ціль: %d голосів
        ⏰ Час: %s
        📅 День: %s
        🔄 Статус: %s""";
    public static final String WEEKLY_TIME_PREFIX = "з ";
    public static final String WEEKLY_STATUS_ENABLED = "Увімкнено";
    public static final String WEEKLY_STATUS_DISABLED = "Вимкнено";
    public static final String BUTTON_TOGGLE_DISABLE = "🔴 Вимкнути";
    public static final String BUTTON_TOGGLE_ENABLE = "🟢 Увімкнути";
    public static final String BUTTON_WEEKLY_DAY = "📅 День";
    public static final String BUTTON_WEEKLY_TIME = "⏰ Час";

    // Weekly Day Page
    public static final String WEEKLY_DAY_TITLE = "Оберіть день тижня для опитування:";
    public static final String BUTTON_BACK = "◀️ Назад";

    // Weekly Time Page
    public static final String WEEKLY_TIME_TITLE = "Введіть годину початку опитування (0-23, у 24-годинному форматі, наприклад: 13):";

    // Common text fragments for tests and UI
    public static final String TEXT_VOTES = "голосів";
    public static final String TEXT_CREATE_POLL = "Створення опитування";
    public static final String TEXT_WEEKLY_SETTINGS = "Тижневі налаштування";
    public static final String TEXT_SELECT_DAY = "Оберіть день тижня";
    public static final String TEXT_ENTER_HOUR = "Введіть годину початку";
    public static final String TEXT_24_HOUR_FORMAT = "24-годинному форматі";
    public static final String TEXT_0_TO_23 = "0-23";

    // Day abbreviations
    public static final String DAY_MONDAY = "Пн";
    public static final String DAY_TUESDAY = "Вт";
    public static final String DAY_WEDNESDAY = "Ср";
    public static final String DAY_THURSDAY = "Чт";
    public static final String DAY_FRIDAY = "Пт";
    public static final String DAY_SATURDAY = "Сб";
    public static final String DAY_SUNDAY = "Нд";

    // Helper method for day conversion
    public static String getDayShort(String day) {
        return switch (day) {
            case "MONDAY" -> DAY_MONDAY;
            case "TUESDAY" -> DAY_TUESDAY;
            case "WEDNESDAY" -> DAY_WEDNESDAY;
            case "THURSDAY" -> DAY_THURSDAY;
            case "FRIDAY" -> DAY_FRIDAY;
            case "SATURDAY" -> DAY_SATURDAY;
            case "SUNDAY" -> DAY_SUNDAY;
            default -> day;
        };
    }

    // Edit prompts for AdminSession
    public static final String PROMPT_EDIT_QUESTION = "📝 Введіть нове питання:";
    public static final String PROMPT_EDIT_POSITIVE = "✅ Введіть новий варіант \"за\":";
    public static final String PROMPT_EDIT_NEGATIVE = "❌ Введіть новий варіант \"проти\":";
    public static final String PROMPT_EDIT_VOTES = "🎯 Введіть нову ціль голосів:";
    public static final String PROMPT_WEEKLY_QUESTION = "📝 Введіть нове тижневе питання:";
    public static final String PROMPT_WEEKLY_POSITIVE = "✅ Введіть новий тижневий варіант \"за\":";
    public static final String PROMPT_WEEKLY_NEGATIVE = "❌ Введіть новий тижневий варіант \"проти\":";
    public static final String PROMPT_WEEKLY_VOTES = "🎯 Введіть нову тижневу ціль голосів:";
    public static final String PROMPT_WEEKLY_DAY = "📅 Виберіть день тижня (1-7):";
    public static final String PROMPT_WEEKLY_TIME = "⏰ Введіть час (ГГ:ХХ):";

    // Additional text fragments for tests
    public static final String TEXT_POLL_CREATION = "Створення опитування";
    public static final String TEXT_CHOOSE_DAY = "Оберіть день тижня";
    public static final String TEXT_RANGE_0_23 = "0-23";
    public static final String TEXT_VOTES_SUFFIX = " голосів";
    public static final String TEXT_TIME_PREFIX_WITH_SPACE = "з ";

    // Default values for WeeklyPollConfig
    public static final String DEFAULT_WEEKLY_QUESTION = "В четвер в 20:00 де завжди?";
    public static final String DEFAULT_POSITIVE_OPTION = "Так, граю!";
    public static final String DEFAULT_NEGATIVE_OPTION = "Ні, не можу";
    public static final int DEFAULT_TARGET_VOTES = 12;

    // Error messages for validation
    public static final String ERROR_DAY_RANGE = "День повинен бути від 1 до 7";
    public static final String ERROR_TIME_FORMAT = "Час повинен бути в форматі ГГ:ХХ";
    public static final String ERROR_HOUR_RANGE = "Години повинні бути від 0 до 23";
    public static final String ERROR_MINUTE_RANGE = "Хвилини повинні бути від 0 до 59";
}
