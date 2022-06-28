package xtvapps.core;

public abstract class Preferences {
    public String getString(String key) {
        return getString(key, null);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public abstract String getString(String key, String defaultValue);
    public abstract boolean getBoolean(String key, boolean defaultValue);

    public abstract PreferencesEditor edit();
}


