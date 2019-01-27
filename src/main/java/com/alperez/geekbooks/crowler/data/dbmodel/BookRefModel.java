package com.alperez.geekbooks.crowler.data.dbmodel;

import com.alperez.geekbooks.crowler.data.IdProvidingModel;
import com.alperez.geekbooks.crowler.data.LongId;
import com.alperez.geekbooks.crowler.utils.Nullable;
import com.google.auto.value.AutoValue;

import java.net.URL;

@AutoValue
public abstract class BookRefModel implements IdProvidingModel {
    public abstract LongId<BookModel> id();
    public abstract String title();
    @Nullable
    public abstract String subtitle();
    public abstract Integer year();
    @Nullable
    public abstract URL imagePath();
}
