package com.alperez.geekbooks.crowler;

import java.net.URL;

public class CategoryItem {

    private final URL url;
    private final int nBooks;

    public CategoryItem(URL url, int nBooks) {
        this.url = url;
        this.nBooks = nBooks;
    }

    public URL getUrl() {
        return url;
    }

    public int getnBooks() {
        return nBooks;
    }
}
