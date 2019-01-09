package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.data.BookRefItem;
import com.alperez.geekbooks.crowler.data.CategoryItem;
import com.alperez.geekbooks.crowler.parser.BookListPageParser;
import com.alperez.geekbooks.crowler.utils.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CategoryItemProcessor implements Runnable {
    public static final String BOOKS_PAGE_CONTENT_TAG = "<table class=\"book-list\">";
    private static final AtomicInteger static_inst_cntr = new AtomicInteger(0);


    public interface CategoryItemProvider {
        @Nullable
        CategoryItem getCategoryItem();
    }

    public interface OnBookFoundListener {
        void onBookFound(Collection<BookRefItem> refItems);
    }

    private final int nInstance;
    private final URL urlHost;
    private final CategoryItemProvider src;
    private final OnBookFoundListener dst;

    private final String TAG;

    public CategoryItemProcessor(URL urlHost, @NonNull CategoryItemProvider src, @NonNull OnBookFoundListener cb) {
        this.urlHost = urlHost;
        this.src = src;
        this.dst = cb;
        nInstance = static_inst_cntr.incrementAndGet();
        TAG = String.format("%s-%d", getClass().getSimpleName(), nInstance);
    }

    @Override
    public void run() {
        CategoryItem runItem;
        while ((runItem = src.getCategoryItem()) != null) {
            Collection<BookRefItem> foundRefs = evaluateCategoryItem(runItem);
            dst.onBookFound(foundRefs);
        }
    }


    private Collection<BookRefItem> evaluateCategoryItem(CategoryItem runItem) {
        List<BookRefItem> allRefs = new LinkedList<>();
        int nBooksFound = 0;
        int nPage = 1;
        do {
            URL pageUrl = new URL(String.format("%s?p=%d", runItem.getUrl(), nPage));
            Log.d("\n"+TAG, String.format("---> Start loading books page %d for %s - %s", nPage, runItem.getTitle(), pageUrl));
            String pageHtml = new HtmlPageLoader(pageUrl).load(1000000);
            Log.d(TAG, String.format("<--- Books page %d has been loaded. Size=%d", nPage, pageHtml.length()));

            Collection<BookRefItem> pageRefs = decodeBooksPage(pageHtml);
            allRefs.addAll(pageRefs);
            Log.d(TAG, String.format("<--- %d book references has been found for %s page %d", pageRefs.size(), runItem.getTitle(), nPage));

            nBooksFound += pageRefs.size();
        } while ((nBooksFound < runItem.getNBooks()) & (nPage++ < 80));
        return allRefs;
    }

    /**
     *
     * @param html content of the list of books
     * @return number of found book references
     * @throws IOException
     */
    private Collection<BookRefItem> decodeBooksPage(String html) throws IOException {
        int index = html.indexOf(BOOKS_PAGE_CONTENT_TAG);
        if (index < 0) {
            throw new IOException(String.format("Bad page content. The %s is not found.", BOOKS_PAGE_CONTENT_TAG));
            //Log.d(Thread.currentThread().getName(), "Bad page content. The %s is not found.", BOOKS_PAGE_CONTENT_TAG);
            //return;
        }

        String content = new XmlTagExtractor(html).getTag("table", index);
        if (content == null) {
            throw new IOException(String.format("Error extract %s content from the initial HTML page", BOOKS_PAGE_CONTENT_TAG));
            //Log.d(Thread.currentThread().getName(), "Error extract %s content from the initial HTML page", BOOKS_PAGE_CONTENT_TAG);
            //return;
        } else {
            Log.d(Thread.currentThread().getName(), "<--- HTML content extracted. Size = "+content.length());
        }

        JSONObject jPage = null;
        try {
            jPage = org.json.XML.toJSONObject(content, false);
            Log.d(TAG, "<--- HTML content was successfully converted to JSON - "+jPage);
        } catch (JSONException e) {
            throw new IOException("cannot convert HTML content to JSON - "+e.getMessage(), e);
        }

        BookListPageParser pageParser = new BookListPageParser(jPage, urlHost.toString());
        return pageParser.parse();
    }
}
