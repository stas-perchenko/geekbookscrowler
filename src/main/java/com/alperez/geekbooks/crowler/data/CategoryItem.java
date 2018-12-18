package com.alperez.geekbooks.crowler.data;

import java.net.URL;

public class CategoryItem {

    private final String title;
    private final URL url;
    private final int nBooks;

    private final int hashCode;
    private final String text;




    public CategoryItem(String title, URL url, int nBooks) {
        this.title = title;
        this.url = url;
        this.nBooks = nBooks;

        hashCode = (text = String.format("{title=%s, count=%d, link=%s}", title, nBooks,url.toString())).hashCode();
    }

    public String getTitle() {
        return title;
    }

    public URL getUrl() {
        return url;
    }

    public int getNBooks() {
        return nBooks;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return text;
    }
}
