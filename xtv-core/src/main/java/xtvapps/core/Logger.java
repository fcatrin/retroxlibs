package xtvapps.core;

public interface Logger {
    void d(String tag, String message);
    void e(String tag, String message);
    void e(String tag, String message, Throwable t);
}
