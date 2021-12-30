package xtvapps.core;

public interface UIThreadExecutor {
    void post(Runnable runnable);
    void post(Runnable runnable, long ms);
}
