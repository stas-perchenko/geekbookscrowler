package com.alperez.geekbooks.crowler.data;

import com.alperez.geekbooks.crowler.utils.Nullable;
import com.alperez.siphash.SipHash;
import com.alperez.siphash.SipHashKey;
import com.google.auto.value.AutoValue;

import java.io.UnsupportedEncodingException;

@AutoValue
public abstract class BookCategoryModel {
    public abstract int level();
    public abstract String title();
    @Nullable
    public abstract BookCategoryModel parent();

    public static BookCategoryModel create(int level, String title, @Nullable BookCategoryModel parent) {
        BookCategoryModel instance = new AutoValue_BookCategoryModel(level, title, parent);
        SipHashKey key = SipHashKey.ofBytes(new byte[]{76, 94, -111, 24, 73, -81, -6, -101, 8, 116, 78, 113, 105, 67, 108, -15});
        try {
            instance.id = SipHash.calculateHash(key, String.format("%d:%s", level, title).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    private long id;

    public long id() {
        return id;
    }
}
