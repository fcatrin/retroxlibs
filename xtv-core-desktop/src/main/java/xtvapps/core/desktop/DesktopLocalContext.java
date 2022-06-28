package xtvapps.core.desktop;

import java.io.File;

import xtvapps.core.LocalContext;
import xtvapps.core.Preferences;
import xtvapps.core.SimpleCallback;

public class DesktopLocalContext extends LocalContext {
    File dataDir;
    File homeDir;

    public DesktopLocalContext(String appName) {
        homeDir = new File(System.getProperty("user.home"));
        dataDir = new File(homeDir, ".local/share/xtv/" + appName);
        dataDir.mkdirs();
    }

    @Override
    public void toast(String message) {

    }

    @Override
    public File getLocalStorage() {
        return null;
    }

    @Override
    public Preferences getPreferences(String name) {
        return new DesktopPreferences(new File(dataDir, name + ".json"));
    }

    @Override
    public void showAlert(String title, String message, SimpleCallback callback) {

    }
}
