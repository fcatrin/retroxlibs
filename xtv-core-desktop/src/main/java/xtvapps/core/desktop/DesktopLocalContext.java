package xtvapps.core.desktop;

import java.io.File;

import xtvapps.core.LocalContext;
import xtvapps.core.Preferences;
import xtvapps.core.SimpleCallback;

public class DesktopLocalContext extends LocalContext {

    @Override
    public void toast(String message) {

    }

    @Override
    public File getLocalStorage() {
        return null;
    }

    @Override
    public Preferences getPreferences(String name) {
        return null;
    }

    @Override
    public void showAlert(String title, String message, SimpleCallback callback) {

    }
}
