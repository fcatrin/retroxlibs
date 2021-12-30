package xtvapps.core;

import xtvapps.core.content.KeyValue;

public class ListOption extends KeyValue {
    final private String preset;

    public ListOption(int index, String value) {
        this(String.valueOf(index), value);
    }

    public ListOption(String key, String value) {
        this(key, value, null);
    }

    public ListOption(String key, String value, String preset) {
        super(key, value);
        this.preset = preset;
    }

    public String getPreset() {
        return preset;
    }

}

