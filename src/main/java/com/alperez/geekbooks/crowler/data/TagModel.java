package com.alperez.geekbooks.crowler.data;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TagModel {

    public abstract long id();
    public abstract String title();

    public static TagModel create(long id, String title) {
        return new AutoValue_TagModel(id, title);
    }

    public TagModel withId(long id) {
        return new AutoValue_TagModel(id, title());
    }

}
