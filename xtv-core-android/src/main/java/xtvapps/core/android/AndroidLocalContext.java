package xtvapps.core.android;

import android.app.Activity;
import android.content.Context;

import java.io.File;

import xtvapps.core.LocalContext;
import xtvapps.core.Preferences;
import xtvapps.core.SimpleCallback;

public class AndroidLocalContext extends LocalContext {
    private final Activity activity;

    public AndroidLocalContext(Activity activity) {
        this.activity = activity;
    }

    public Context getAndroidContext() {
        return activity;
    }
    public Activity getActivity() { return activity;}

    @Override
    public void toast(String message) {
        AndroidCoreUtils.toast(activity, message);
    }

    @Override
    public File getLocalStorage() {
        return AndroidCoreUtils.getCacheExternalStorage(activity);
    }

    @Override
    public Preferences getPreferences(String name) {
        return new AndroidPreferences(activity.getSharedPreferences(name, Context.MODE_PRIVATE));
    }

    @Override
    public void showAlert(String title, String message, SimpleCallback callback) {

    }
}
