package xtvapps.core.android;

import android.content.SharedPreferences;

import xtvapps.core.Preferences;
import xtvapps.core.PreferencesEditor;

public class AndroidPreferences implements Preferences {
    private final SharedPreferences sharedPreferences;

    public AndroidPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    @Override
    public PreferencesEditor edit() {
        return new AndroidPreferencesEditor(sharedPreferences.edit());
    }

}
