package com.alperez.geekbooks.crowler.data;

import com.alperez.geekbooks.crowler.utils.Nullable;
import com.alperez.siphash.SipHash;
import com.alperez.siphash.SipHashKey;
import com.google.auto.value.AutoValue;

import java.io.UnsupportedEncodingException;

@AutoValue
public abstract class BookCategoryModel implements IdProvidingModel, Cloneable {
    public abstract int level();
    public abstract String title();
    @Nullable
    public abstract BookCategoryModel parent();

    public static BookCategoryModel create(int level, String title, @Nullable BookCategoryModel parent) {
        BookCategoryModel instance = new AutoValue_BookCategoryModel(level, title, parent);
        SipHashKey key = SipHashKey.ofBytes(new byte[]{76, 94, -111, 24, 73, -81, -6, -101, 8, 116, 78, 113, 105, 67, 108, -15});
        String hashText = (parent == null)
                ? String.format("%d:%s", level, title)
                : String.format("%d:%d:%s", parent.id(), level, title);
        try {
            long idValue = SipHash.calculateHash(key, hashText.getBytes("UTF-8")) >>> 1;
            instance.id = LongId.valueOf(idValue);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    private LongId<BookCategoryModel> id;

    public LongId<BookCategoryModel> id() {
        return id;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        buildString(sb);
        return sb.toString();
    }


    private void buildString(StringBuilder sb) {
        if (parent() != null) {
            parent().buildString(sb);
        }

        if (sb.length() > 0) sb.append("->");
        sb.append(title());
    }

    @Override
    protected BookCategoryModel clone() {
        BookCategoryModel p = parent();
        BookCategoryModel clonedP = (p == null) ? null : p.clone();
        return BookCategoryModel.create(level(), title(), clonedP);
    }
}
