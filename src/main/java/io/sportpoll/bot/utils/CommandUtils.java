package io.sportpoll.bot.utils;

public class CommandUtils {
    public static boolean isCommand(String messageText) {
        if (messageText == null || messageText.trim().isEmpty()) {
            return false;
        }
        return messageText.trim().startsWith("/");
    }

    public static String parseCommand(String messageText) {
        if (!isCommand(messageText)) {
            return "";
        }
        String normalized = normalizeCommand(messageText);
        return normalized.trim().split("\\s+")[0];
    }

    public static String[] parseArguments(String messageText) {
        if (!isCommand(messageText)) {
            return new String[0];
        }
        String normalized = normalizeCommand(messageText);
        String[] parts = normalized.trim().split("\\s+");
        if (parts.length <= 1) return new String[0];
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        return args;
    }

    private static String normalizeCommand(String messageText) {
        String trimmed = messageText.trim();
        if (trimmed.matches("/\\+\\d+")) {
            return trimmed.replaceFirst("(\\+)(\\d+)", "$1 $2");
        }
        if (trimmed.matches("/-\\d+")) {
            return trimmed.replaceFirst("(-)(\\d+)", "$1 $2");
        }
        if (trimmed.matches("/\\+[A-Za-z].*")) {
            return trimmed.replaceFirst("(\\+)([A-Za-z])", "$1 $2");
        }
        if (trimmed.matches("/-[A-Za-z].*")) {
            return trimmed.replaceFirst("(-)([A-Za-z])", "$1 $2");
        }
        return trimmed;
    }
}
