package xtvapps.core.android;

import android.os.Handler;

import xtvapps.core.UIThreadExecutor;

public class AndroidUIThreadExecutor implements UIThreadExecutor {
    final Handler handler;

    public AndroidUIThreadExecutor(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void post(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public void post(Runnable runnable, long ms) {
        handler.postDelayed(runnable, ms);
    }
}
