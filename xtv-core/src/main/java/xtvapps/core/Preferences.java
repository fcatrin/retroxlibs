package xtvapps.core;

public interface Preferences {
    String getString(String key, String defaultValue);
    boolean getBoolean(String key, boolean defaultValue);

    PreferencesEditor edit();
}


