package xtvapps.core.android;

import android.content.SharedPreferences;

import xtvapps.core.PreferencesEditor;

public class AndroidPreferencesEditor implements PreferencesEditor {

    private final SharedPreferences.Editor editor;

    public AndroidPreferencesEditor(SharedPreferences.Editor editor) {
        this.editor = editor;
    }

    @Override
    public void putString(String key, String value) {
        editor.putString(key, value);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
    }

    @Override
    public void apply() {
        editor.apply();
    }
}
