package xtvapps.core.desktop;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import xtvapps.core.FileUtils;
import xtvapps.core.Preferences;
import xtvapps.core.PreferencesEditor;

public class DesktopPreferences extends Preferences {
    private final File prefsFile;
    private JSONObject prefs;

    public DesktopPreferences(File prefsFile) {
        this.prefsFile = prefsFile;
        load();
    }
    @Override
    public String getString(String key, String defaultValue) {
        return prefs.optString(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.optBoolean(key, defaultValue);
    }

    @Override
    public PreferencesEditor edit() {
        return new DesktopPreferencesEditor(this, prefs);
    }

    private void load() {
        prefs = new JSONObject();
        if (prefsFile.exists()) {
            try {
                String sPrefs = FileUtils.loadString(prefsFile);
                prefs = new JSONObject(sPrefs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void set(JSONObject prefs) {
        try {
            FileUtils.saveString(prefsFile, prefs.toString());
            load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
