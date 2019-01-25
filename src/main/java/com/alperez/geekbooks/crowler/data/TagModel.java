package com.alperez.geekbooks.crowler.data;

import com.alperez.geekbooks.crowler.utils.Nullable;
import com.google.auto.value.AutoValue;


@AutoValue
public abstract class TagModel implements IdProvidingModel {
    @Nullable
    public abstract LongId<TagModel> id();
    public abstract String title();

    public static TagModel create(@Nullable LongId<TagModel> id, String title) {
        return new AutoValue_TagModel(id, title);
    }

    public TagModel withId(LongId<TagModel> id) {
        return new AutoValue_TagModel(id, title());
    }

}
