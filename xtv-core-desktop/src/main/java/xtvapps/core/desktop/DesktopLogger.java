package xtvapps.core.desktop;

import xtvapps.core.Logger;

public class DesktopLogger implements Logger {

    @Override
    public void d(String tag, String message) {
        System.out.println(String.format("[%s] %s", tag, message));
    }

    @Override
    public void e(String tag, String message) {
        System.err.println(String.format("[%s] %s", tag, message));
    }

    @Override
    public void e(String tag, String message, Throwable throwable) {
        System.err.println(String.format("[%s] %s", tag, message));
        throwable.printStackTrace();
    }
}
