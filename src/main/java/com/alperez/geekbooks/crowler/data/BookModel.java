package com.alperez.geekbooks.crowler.data;

import com.alperez.geekbooks.crowler.utils.Nullable;
import com.google.auto.value.AutoValue;

import java.net.URL;
import java.util.List;

@AutoValue
public abstract class BookModel {
    public abstract URL geekBooksAddress();
    @Nullable
    public abstract URL imagePath();
    public abstract URL pdfPath();
    public abstract Float pdfSize();
    public abstract String title();
    @Nullable
    public abstract String subtitle();
    public abstract List<AuthorModel> authors();
    public abstract Integer year();
    public abstract Integer numPages();
    public abstract String description();
    public abstract List<TagModel> tags();
    public abstract List<BookCategoryModel> category();
}
