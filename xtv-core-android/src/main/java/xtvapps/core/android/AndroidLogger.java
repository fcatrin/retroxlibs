package xtvapps.core.android;

import android.util.Log;

import xtvapps.core.Logger;

@SuppressWarnings("unused")
public class AndroidLogger implements Logger {

    @Override
    public void d(String tag, String message) {
        Log.d(tag, message);
    }

    @Override
    public void e(String tag, String message) {
        Log.e(tag, message);
    }

    @Override
    public void e(String tag, String message, Throwable t) {
        Log.e(tag, message, t);
    }
}
