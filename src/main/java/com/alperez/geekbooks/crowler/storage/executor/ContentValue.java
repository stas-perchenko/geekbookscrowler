package com.alperez.geekbooks.crowler.storage.executor;

import java.util.*;

public final class ContentValue {

    private final Map<String, Object> data;
    private final List<String> keys;

    public ContentValue() {
        this(0);
    }

    public ContentValue(int capacity) {
        data = new HashMap<>(capacity);
        keys = new ArrayList<>(capacity);
    }

    public List<String> getKeys() {
        return new ArrayList<>(keys);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public void put(String key, Object o) {
        if (data.put(key, o) == null) {
            keys.add(key);
        }
    }

    public void put(String key, String s) {
        put(key, (Object) s);
    }

    public void put(String key, int i) {
        if (data.put(key, Integer.toString(i)) == null) {
            keys.add(key);
        }
    }

    public void put(String key, long l) {
        if (data.put(key, Long.toString(l)) == null) {
            keys.add(key);
        }
    }

    public void put(String key, float f) {
        if (data.put(key, Float.toString(f)) == null) {
            keys.add(key);
        }
    }

    public void put(String key, double d) {
        if (data.put(key, Double.toString(d)) == null) {
            keys.add(key);
        }
    }
}
