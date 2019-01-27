package com.alperez.geekbooks.crowler.data.dbmodel;

import com.alperez.geekbooks.crowler.data.IdProvidingModel;
import com.alperez.geekbooks.crowler.data.LongId;
import com.alperez.geekbooks.crowler.data.TagModel;
import com.alperez.geekbooks.crowler.utils.NonNull;
import com.alperez.geekbooks.crowler.utils.Nullable;
import com.alperez.geekbooks.crowler.utils.TextUtils;
import com.alperez.siphash.SipHash;
import com.alperez.siphash.SipHashKey;
import com.google.auto.value.AutoValue;
import com.sun.javafx.UnmodifiableArrayList;

import java.awt.geom.Dimension2D;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

@AutoValue
public abstract class BookModel implements IdProvidingModel {
    public abstract URL geekBooksAddress();
    @Nullable
    public abstract URL imagePath();
    @Nullable
    public abstract Dimension2D imageDimensions();
    public abstract URL origPdfPath();
    public abstract Float pdfSize();
    @Nullable
    public abstract String finPdfFileName();
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
    public abstract List<LongId<BookModel>> relatedBookIds();

    private LongId<BookModel> id;

    public LongId<BookModel> id() {
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
        public abstract Builder setImageDimensions(@Nullable Dimension2D imageDimensions);
        public abstract Builder setOrigPdfPath(URL pdfPath);
        public abstract Builder setPdfSize(Float pdfSize);
        public abstract Builder setFinPdfFileName(@Nullable String finPdfFileName);
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
        public abstract Builder setRelatedBookIds(List<LongId<BookModel>> relatedBookIds);
        abstract BookModel actualBuild();
        public BookModel build() {
            BookModel instance = actualBuild();
            SipHashKey key = SipHashKey.ofBytes(new byte[]{-20, 109, -21, 99, -27, 19, -51, 96, 71, 31, 96, -34, 1, -83, 3, 117});
            try {
                long idValue = SipHash.calculateHash(key, instance.getIdHashText().getBytes("UTF-8")) >>> 1;
                instance.id = LongId.valueOf(idValue);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            return instance;
        }

    }

    public BookModel withRelatedBookIds(@NonNull List<LongId<BookModel>> ids) {
        AuthorModel new_authors[] = authors().toArray(new AuthorModel[authors().size()]);
        TagModel new_tags[] = tags().toArray(new TagModel[tags().size()]);
        return builder()
                .setGeekBooksAddress(geekBooksAddress())
                .setImagePath(imagePath())
                .setImageDimensions(imageDimensions())
                .setOrigPdfPath(origPdfPath())
                .setPdfSize(pdfSize())
                .setIsbn(isbn())
                .setAsin(asin())
                .setTitle(title())
                .setSubtitle(subtitle())
                .setAuthors(new UnmodifiableArrayList<>(new_authors, new_authors.length))
                .setYear(year())
                .setNumPages(numPages())
                .setDescription(description())
                .setTags(new UnmodifiableArrayList<>(new_tags, new_tags.length))
                .setCategory(category().clone())
                .setRelatedBookIds(ids)
                .build();

    }



}
