package xtvapps.core.desktop;

import org.json.JSONObject;

import xtvapps.core.PreferencesEditor;

public class DesktopPreferencesEditor implements PreferencesEditor {

    private final JSONObject prefs;
    private final DesktopPreferences preferences;

    public DesktopPreferencesEditor(DesktopPreferences preferences, JSONObject prefs) {
        this.preferences = preferences;
        this.prefs = new JSONObject(prefs.toString());
    }

    @Override
    public void putString(String key, String value) {
        prefs.put(key, value);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        prefs.put(key, value);
    }

    @Override
    public void apply() {
        preferences.set(prefs);
    }
}
