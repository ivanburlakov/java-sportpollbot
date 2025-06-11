package io.sportpoll.bot.constants;

public class Messages {
    public static final String ERROR = "❌ %s";
    public static final String SUCCESS = "✅ %s";
    public static final String INFO = "ℹ️ %s";
    public static final String INPUT_REQUEST = "Введіть %s:";

    public static final String DEFAULT_ERROR = "Упс! Шось не так 🫠";
    public static final String NO_ACTIVE_POLL = "Немає активного опитування";
    public static final String POLL_CLOSED = "Опитування закрито";

    public static final String VOTE_ADDED = "Додано %s";
    public static final String VOTE_REVOKED = "Голос скасовано";
    public static final String NO_VOTES_TO_REVOKE = "Немає голосів для скасування";
    public static final String TOO_MANY_VOTES = "Неможливо додати %d голосів. Залишилось лише %d до досягнення цілі";
    public static final String ADMIN_REVOKED_VOTE = "Адмін скасував голос #%s (%s від %s)";
    public static final String VOTE_REVOKED_BY_ID = "Голос #%s (%s) скасовано";
    public static final String PERMISSION_DENIED_REVOKE = "Ви можете скасувати лише свої голоси";
    public static final String VOTE_NOT_FOUND = "Голос не знайдено";
    public static final String REVOKE_USAGE = "Використання: /- [номер]";
    public static final String REQUIRED_ENV_VAR = "Обов'язкова змінна середовища не встановлена: %s";

    public static final String INVALID_INPUT = "Неправильний ввід. %s";
    public static final String ENTER_TARGET_VOTES = "цільову кількість голосів";
    public static final String ENTER_POLL_QUESTION = "питання для опитування";
    public static final String ENTER_POSITIVE_OPTION = "текст для варіанту \"за\"";
    public static final String ENTER_NEGATIVE_OPTION = "текст для варіанту \"проти\"";

    public static final String OPTION_SET = "%s встановлено: %s";
    public static final String SESSION_ENDED = "Сесію завершено. Використовуйте /plan або перешліть опитування";
    public static final String OPERATION_CANCELLED = "Операцію скасовано";

    public static final String WELCOME = "Ласкаво просимо! Використовуйте /help";
    public static final String ADMIN_HELP = "/plan - Нове опитування\n/weekly - Щотижневе\n/status - Статус\n/close - Закрити\n/abort - Скасувати\n/reboot - Перезапуск";
    public static final String GROUP_HELP = "/+ [число/імена] - Голосувати\n/- [номер] - Скасувати голос\n/help - Довідка";

    public static final String BOT_STARTED = "SportPollBot запущено!";
    public static final String REBOOT_STATUS = "🔄 %s";
    public static final String REBOOT_CLEARING_DATA = "Очищення даних...";
    public static final String REBOOT_COMPLETED = "Бот перезапущено";

    public static final String POLL_CREATED = "Опитування створено";
    public static final String INVITED = "запросив %s";

    // Vote display strings
    public static final String ANONYMOUS_VOTE_SINGLE = "1 анонім";
    public static final String ANONYMOUS_VOTES_MULTIPLE = "x%d аноніми";
    public static final String NAMED_VOTE_SINGLE = "голос за %s";
    public static final String NAMED_VOTES_MULTIPLE = "голоси за: %s";
    public static final String ANONYMOUS_VOTER = "Анонім";

    // Legacy status strings (used in sendPollStatus)
    public static final String STATUS_COMPLETED = "📊 <b>ЗАВЕРШЕНО</b>\n\n";
    public static final String STATUS_ACTIVE = "📊 <b>АКТИВНЕ</b>\n\n";
    public static final String STATUS_GOAL = "Ціль: %d голосів за\n";
    public static final String STATUS_CURRENT = "Поточний стан: %d голосів за\n";
    public static final String STATUS_POSITIVE_VOTES = "\nГолоси за:\n";

    // Status message strings
    public static final String STATUS_HEADER = "📊 <b>Статус опитування</b>\n\n";
    public static final String STATUS_POLL_COMPLETED = "✅ <b>Опитування завершено!</b>\n";
    public static final String STATUS_POLL_ACTIVE = "🗳️ <b>Опитування активне</b>\n";
    public static final String STATUS_TARGET = "🎯 Ціль: <b>%d</b> голосів\n";
    public static final String STATUS_REMAINING = "⏳ Залишилось: <b>%d</b> голосів\n";
    public static final String STATUS_HOW_TO_VOTE = "\n💡 <b>Як голосувати:</b>\n";
    public static final String STATUS_USE_POLL_ABOVE = "• Використовуйте опитування вище для особистого голосу\n";
    public static final String STATUS_OR_USE_COMMANDS = "• Або використовуйте команди:\n";
    public static final String STATUS_COMMAND_PLUS = "  <code>/+</code> - додати 1 голос ЗА\n";
    public static final String STATUS_COMMAND_PLUS_NUMBER = "  <code>/+ 3</code> - додати 3 голоси ЗА\n";
    public static final String STATUS_COMMAND_PLUS_NAMES = "  <code>/+ Петро Іван</code> - додати голоси від імені людей\n";
    public static final String STATUS_COMMAND_MINUS = "  <code>/- [номер]</code> - відкликати голос за номером зі списку\n\n";
    public static final String STATUS_VOTE_LIST = "📋 <b>Список голосів ЗА:</b>\n";

    // Error messages for revokeVoteByNumber
    public static final String INVALID_VOTE_NUMBER = "Номер голосу повинен бути більше 0";
    public static final String VOTE_NOT_FOUND_BY_NUMBER = "Голос з номером %d не знайдено";
    public static final String ADMIN_REVOKED_DIRECT_VOTE = "Адмін відкликав голос #%d (%s)";
    public static final String REVOKED_DIRECT_VOTE = "Голос #%d (%s) відкликано";
    public static final String ADMIN_REVOKED_EXTERNAL_VOTE = "Адмін відкликав голос #%d (%s від %s)";
    public static final String REVOKED_EXTERNAL_VOTE = "Голос #%d (%s) відкликано";

    // Pin message logs
    public static final String PIN_SUCCESS = "✅ Status message pinned successfully";
    public static final String PIN_ERROR_RIGHTS = "❌ Bot needs admin rights with 'Pin Messages' permission in the group";
    public static final String PIN_ERROR_PROMOTE = "   Please promote the bot to admin in group: %d";
    public static final String PIN_ERROR_GENERAL = "Failed to pin status message: %s";
    public static final String UNPIN_ERROR = "Failed to unpin status message: %s";

    // Group chat messages
    public static final String GROUP_WELCOME = "👋 Привіт! Я SportPollBot - ваш помічник для організації опитувань.\n\n🗳️ Використовуйте команди:\n• /+ - проголосувати ЗА поточне опитування\n• /- - відкликати свій голос\n• /help - показати цю довідку\n\n🔧 Адміністратори можуть створювати опитування через особисті повідомлення.";

    // Non-admin user messages
    public static final String USER_WELCOME = "👋 Привіт, %s!\n\nЯ SportPollBot - бот для організації спортивних опитувань.\n\n🏃‍♂️ Приєднуйтесь до групи, щоб брати участь в опитуваннях!\n🗳️ Голосуйте за спортивні активності\n📊 Слідкуйте за результатами\n\nВикористовуйте /help для довідки.";
    public static final String USER_HELP = "❓ <b>Довідка SportPollBot</b>\n\n<b>Для звичайних користувачів:</b>\n• Приєднуйтесь до групи для участі\n• Використовуйте /+ для голосування ЗА\n• Використовуйте /- для відкликання голосу\n\n<b>Контакти:</b>\nЯкщо у вас є питання, зверніться до адміністратора групи.";
    public static final String USER_UNKNOWN_COMMAND = "🤔 Невідома команда.\n\nДоступні команди:\n• /start - почати роботу\n• /help - показати довідку";
    public static final String USER_DEFAULT_MESSAGE = "👋 Привіт! Використовуйте команди або приєднуйтесь до групи для участі в опитуваннях.\n\nНаберіть /help для довідки.";

    // Admin help messages
    public static final String ADMIN_HELP_TEXT = "🔧 <b>Команди адміністратора</b>\n\n• /start - відкрити панель адміністратора\n• /help - показати цю довідку\n• /reboot - перезавантажити систему\n\nВикористовуйте /start для доступу до всіх функцій.";

    // Poll completion message
    public static final String POLL_COMPLETION_MESSAGE = "🏆 Місця на цей тиждень зайняті. Побачимось наступного!";
}
