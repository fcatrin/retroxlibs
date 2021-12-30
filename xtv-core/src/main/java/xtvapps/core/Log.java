package xtvapps.core;

import java.util.HashSet;

public class Log {
    private static boolean enabled = true;
    private final static HashSet<String> whiteList = new HashSet<>();
    private final static HashSet<String> blackList = new HashSet<>();

    public static void setEnabled(boolean enabled) {
        Log.enabled = enabled;
    }

    public static void setEnabled(String logtag) {
        whiteList.add(logtag);
    }

    public static void setDisabled(String logtag) {
        blackList.add(logtag);
    }

    private static boolean isEnabled(String logtag) {
        return (enabled || whiteList.contains(logtag)) && !blackList.contains(logtag);
    }

    public static void d(String tag, String message) {
        if (isEnabled(tag)) AppContext.logger.d(tag, message);
    }

    public static void e(String tag, String message) {
        if (isEnabled(tag)) AppContext.logger.e(tag, message);
    }

    public static void e(String tag, String message, Throwable t) {
        if (isEnabled(tag)) AppContext.logger.e(tag, message, t);
    }

}
