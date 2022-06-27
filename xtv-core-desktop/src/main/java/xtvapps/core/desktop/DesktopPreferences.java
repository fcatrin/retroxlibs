package xtvapps.core.desktop;

import xtvapps.core.Preferences;
import xtvapps.core.PreferencesEditor;

public class DesktopPreferences implements Preferences {
    @Override
    public String getString(String key, String defaultValue) {
        return null;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return false;
    }

    @Override
    public PreferencesEditor edit() {
        return null;
    }
}
