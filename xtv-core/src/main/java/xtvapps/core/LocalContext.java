package xtvapps.core;

import java.io.File;

public abstract class LocalContext {
    public abstract void toast(String message);

    public abstract File getLocalStorage();

    public abstract Preferences getPreferences(String name);

    public void showAlert(String message) {
        showAlert(null, message, null);
    }
    public abstract void showAlert(String title, String message, SimpleCallback callback);
}
