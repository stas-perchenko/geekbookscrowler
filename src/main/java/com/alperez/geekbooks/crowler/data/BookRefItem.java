package com.alperez.geekbooks.crowler.data;

import java.net.URL;

public class BookRefItem {
    private final String title;
    private final URL url;

    private final int hashCode;
    private final String text;

    public BookRefItem(String title, URL url) {
        this.title = title;
        this.url = url;

        hashCode = (text = String.format("{title=%s, link=%s}", title, url.toString())).hashCode();
    }


    public String getTitle() {
        return title;
    }

    public URL getUrl() {
        return url;
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
