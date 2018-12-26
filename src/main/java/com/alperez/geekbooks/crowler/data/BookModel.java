package com.alperez.geekbooks.crowler.data;

import com.alperez.geekbooks.crowler.utils.Nullable;
import com.alperez.geekbooks.crowler.utils.TextUtils;
import com.alperez.siphash.SipHash;
import com.alperez.siphash.SipHashKey;
import com.google.auto.value.AutoValue;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

@AutoValue
public abstract class BookModel {
    public abstract URL geekBooksAddress();
    @Nullable
    public abstract URL imagePath();
    public abstract URL pdfPath();
    public abstract Float pdfSize();
    @Nullable
    public abstract String isbn();
    @Nullable
    public abstract String asin();
    public abstract String title();
    @Nullable
    public abstract String subtitle();
    public abstract List<AuthorModel> authors();
    public abstract Integer year();
    public abstract Integer numPages();
    public abstract String description();
    public abstract List<TagModel> tags();
    public abstract BookCategoryModel category();

    private long id;

    public long id() {
        return id;
    }

    private String getIdHashText() {
        if (TextUtils.isNotEmpty(isbn())) {
            return String.format("%s:%d", isbn().replaceAll("-", ""), year().intValue());
        } else {
            StringBuilder sb = new StringBuilder(year().toString());
            sb.append(':');
            sb.append(title());
            for (AuthorModel author : authors()) {
                sb.append(':');
                sb.append(author.fullName());
            }
            return sb.toString().replaceAll(" ", "_");
        }
    }


    public static Builder builder() {
        return new AutoValue_BookModel.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setGeekBooksAddress(URL geekBooksAddress);
        public abstract Builder setImagePath(@Nullable URL imagePath);
        public abstract Builder setPdfPath(URL pdfPath);
        public abstract Builder setPdfSize(Float pdfSize);
        public abstract Builder setIsbn(@Nullable String isbn);
        public abstract Builder setAsin(@Nullable String asin);
        public abstract Builder setTitle(String title);
        public abstract Builder setSubtitle(@Nullable String subtitle);
        public abstract Builder setAuthors(List<AuthorModel> authors);
        public abstract Builder setYear(Integer year);
        public abstract Builder setNumPages(Integer numPages);
        public abstract Builder setDescription(String description);
        public abstract Builder setTags(List<TagModel> tags);
        public abstract Builder setCategory(BookCategoryModel category);


        abstract BookModel actualBuild();

        public BookModel build() {
            BookModel instance = actualBuild();
            SipHashKey key = SipHashKey.ofBytes(new byte[]{-20, 109, -21, 99, -27, 19, -51, 96, 71, 31, 96, -34, 1, -83, 3, 117});
            try {
                instance.id = SipHash.calculateHash(key, instance.getIdHashText().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            return instance;
        }

    }



}
