package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.data.parsing.BookRefItem;
import com.alperez.geekbooks.crowler.data.parsing.CategoryItem;
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
            String pageHtml = loadCategoryPage(runItem, nPage);

            if (pageHtml != null) {
                try {
                    Collection<BookRefItem> pageRefs = decodeBooksPage(pageHtml);
                    allRefs.addAll(pageRefs);
                    nBooksFound += pageRefs.size();
                    //TODO Log success - Log.d(TAG, String.format("<--- %d book references has been found for %s page %d", pageRefs.size(), runItem.getTitle(), nPage));
                } catch (IOException e) {
                    //TODO Log Error
                    e.printStackTrace(System.out);
                    e.printStackTrace(System.err);
                }
            }

        } while ((nBooksFound < runItem.getNBooks()) & (nPage++ < 50));
        return allRefs;
    }

    @Nullable
    private String loadCategoryPage(CategoryItem category, int nPage) {
        try {
            URL pageUrl = new URL(String.format("%s?p=%d", category.getUrl(), nPage));
            //TODO log start - Log.d("\n"+TAG, String.format("---> Start loading books page %d for %s - %s", nPage, category.getTitle(), pageUrl));

            String html = (new HtmlPageLoader(pageUrl)).load(1000000);
            //TODO log end - Log.d(TAG, String.format("<--- Books page %d has been loaded. Size=%d", nPage, pageHtml.length()));
            return html;
        } catch (IOException e) {
            //TODO Log error
            e.printStackTrace(System.out);
            e.printStackTrace(System.err);
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
        try {
            return pageParser.parse();
        } catch (JSONException e) {
            throw new IOException("cannot parse category page JSON to Book reference items - "+e.getMessage(), e);
        }
    }
}
