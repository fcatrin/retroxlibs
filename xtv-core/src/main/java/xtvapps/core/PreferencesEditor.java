package xtvapps.core;

public interface PreferencesEditor {
    void putString(String key, String value);
    void putBoolean(String key, boolean value);

    void apply();
}
