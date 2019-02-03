package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.data.parsing.BookRefItem;
import com.alperez.geekbooks.crowler.data.parsing.CategoryItem;
import com.alperez.geekbooks.crowler.parser.BookListPageParser;
import com.alperez.geekbooks.crowler.utils.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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

    private final List<Log.LogEntry> mLogs = new LinkedList<>();

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
            Log.d(mLogs);
            mLogs.clear();
            dst.onBookFound(foundRefs);
        }
    }


    private Collection<BookRefItem> evaluateCategoryItem(CategoryItem runItem) {
        List<BookRefItem> allRefs = new LinkedList<>();
        int nBooksFound = 0;
        int nPage = 1;
        do {
            String title = String.format("\"%s\", page %d", runItem.getTitle(), nPage);
            mLogs.add(new Log.LogEntry(TAG, "\n--->  Start processing "+title));
            String pageHtml = loadCategoryPage(runItem, nPage);

            if (pageHtml != null) {
                try {
                    Collection<BookRefItem> pageRefs = decodeBooksPage(pageHtml);
                    allRefs.addAll(pageRefs);
                    nBooksFound += pageRefs.size();
                    mLogs.add(new Log.LogEntry(TAG, String.format("<--- %d book references has been found for %s page %d", pageRefs.size(), runItem.getTitle(), nPage)));
                } catch (IOException e) {
                    mLogs.add(new Log.LogEntry(TAG, e));
                }
            } else {
                mLogs.add(new Log.LogEntry(TAG, "<~~~ Error load HTML data of the "+title));
            }

        } while ((nBooksFound < runItem.getNBooks()) & (nPage++ < 50));
        return allRefs;
    }

    @Nullable
    private String loadCategoryPage(CategoryItem category, int nPage) {
        try {
            URL pageUrl = new URL(String.format("%s?p=%d", category.getUrl(), nPage));

            String html = (new HtmlPageLoader(pageUrl)).load(1000000);
            return html;
        } catch (IOException e) {
            mLogs.add(new Log.LogEntry(TAG, e));
            return null;
        }
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
        }

        String content = new XmlTagExtractor(html).getTag("table", index);
        if (content == null) {
            throw new IOException(String.format("Error extract %s content from the initial HTML page", BOOKS_PAGE_CONTENT_TAG));
        }

        JSONObject jPage;
        try {
            jPage = org.json.XML.toJSONObject(content, false);
        } catch (JSONException e) {
            throw new IOException("cannot convert HTML content to JSON - "+e.getMessage(), e);
        }

        BookListPageParser pageParser = new BookListPageParser(jPage, urlHost.toString());
        try {
            return pageParser.parse();
        } catch (JSONException e) {
            throw new IOException("cannot parse category page JSON to Book reference items - "+e.getMessage(), e);
        }
    }
}
