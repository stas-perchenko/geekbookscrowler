package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.utils.HtmlPageLoader;
import com.alperez.geekbooks.crowler.utils.Log;
import com.alperez.geekbooks.crowler.utils.TagExtractor;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

public class CategoryProcessor implements Runnable {
    public static final String BOOKS_PAGE_CONTENT_TAG = "<div class=\"clearfix content category view\">";


    private static final AtomicInteger static_inst_cntr = new AtomicInteger(0);


    private final int nInstance;
    private final Deque<CategoryItem> itemsQueue;

    private final String TAG;

    public CategoryProcessor(Deque<CategoryItem> itemsQueue) {
        this.itemsQueue = itemsQueue;
        nInstance = static_inst_cntr.incrementAndGet();
        TAG = String.format("%s-%d", getClass().getSimpleName(), nInstance);
    }

    @Override
    public void run() {
        Log.d(TAG, "---> run() start");
        try {
            synchronized (itemsQueue) {
                if (itemsQueue.isEmpty()) {
                    try {
                        itemsQueue.wait(45000);
                    } catch (InterruptedException e) {

                    }
                    if (itemsQueue.isEmpty()) return;
                }
            }

            for (;;) {
                CategoryItem runItem = null;
                synchronized (itemsQueue) {
                    if (itemsQueue.isEmpty()) {
                        return;
                    } else {
                        runItem = itemsQueue.removeFirst();
                    }
                }

                try {
                    evaluateCategoryItem(runItem);
                } catch (IOException e) {
                    Log.d(TAG, "<~~~  Error evaluate Category Item - "+e.getMessage());
                    e.printStackTrace();
                }
            }
        } finally {
            Log.d(TAG, "<--- run() end");
        }
    }


    private void evaluateCategoryItem(CategoryItem runItem) throws IOException {
        int nBooksFound = 0;
        int nPage = 1;
        do {
            URL pageUrl = new URL(String.format("%s?p=%d", runItem.getUrl(), nPage));
            Log.d("\n"+TAG, String.format("---> Start loading books page %d for %s - %s", nPage, runItem.getTitle(), pageUrl));
            String pageHtml = new HtmlPageLoader(pageUrl).load(1000000);
            Log.d(TAG, String.format("<--- Books page %d has been loaded. Size=%d", nPage, pageHtml.length()));
            int pgBooks = decodeBooksPage(pageHtml);
            nBooksFound += pgBooks;
        } while ((nBooksFound < runItem.getNBooks()) & (nPage++ < 80));
    }

    private int decodeBooksPage(String html) throws IOException {
        int index = html.indexOf(BOOKS_PAGE_CONTENT_TAG);
        if (index < 0) {
            throw new IOException(String.format("Bad page content. The %s is not found.", BOOKS_PAGE_CONTENT_TAG));
            //Log.d(Thread.currentThread().getName(), "Bad page content. The %s is not found.", BOOKS_PAGE_CONTENT_TAG);
            //return;
        }

        String content = new TagExtractor(html).getTag("div", index);
        if (content == null) {
            throw new IOException(String.format("Error extract %s content from the initial HTML page", BOOKS_PAGE_CONTENT_TAG));
            //Log.d(Thread.currentThread().getName(), "Error extract %s content from the initial HTML page", BOOKS_PAGE_CONTENT_TAG);
            //return;
        } else {
            Log.d(Thread.currentThread().getName(), "<--- HTML content extracted. Size = "+content.length());
        }

        JSONObject jPage = null;
        try {
            jPage = org.json.XML.toJSONObject(content, true);
            Log.d(TAG, "<--- HTML content was successfully converted to JSON - "+jPage);
        } catch (JSONException e) {
            throw new IOException("cannot convert HTML content to JSON - "+e.getMessage(), e);
        }

        //TODO Implement further
        return 10;
    }
}
