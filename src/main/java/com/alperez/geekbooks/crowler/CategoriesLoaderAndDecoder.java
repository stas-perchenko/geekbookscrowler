package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.data.parsing.BookRefItem;
import com.alperez.geekbooks.crowler.data.parsing.CategoryItem;
import com.alperez.geekbooks.crowler.parser.CategoryIndexParser;
import com.alperez.geekbooks.crowler.utils.HtmlPageLoader;
import com.alperez.geekbooks.crowler.utils.Log;
import com.alperez.geekbooks.crowler.utils.NonNull;
import com.alperez.geekbooks.crowler.utils.XmlTagExtractor;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CategoriesLoaderAndDecoder {
    public static final int STATE_CREATED = 0;
    public static final int STATE_SEARCH_CATEGORIES = 1;
    public static final int STATE_SEARCH_BOOKS = 2;
    public static final int STATE_COMPLETED = 3;


    //--- Source and parameters ---
    private final int nThreads;
    private final URL urlStartPage;
    private final URL urlHost;

    //--- Workers and state ---
    private final AtomicInteger state = new AtomicInteger(STATE_CREATED);
    private final ExecutorService exec;

    //--- Result ---
    private final Deque<CategoryItem> categoryItems = new LinkedList<>();
    private final Set<BookRefItem> foundBookRefs = new HashSet<>();


    public CategoriesLoaderAndDecoder(@NonNull URL urlStartPage, int nThreads) throws MalformedURLException {
        this.urlStartPage = urlStartPage;
        this.urlHost = new URL(String.format("%s://%s", urlStartPage.getProtocol(), urlStartPage.getHost()));
        exec = Executors.newFixedThreadPool(this.nThreads = nThreads);
    }

    public int getState() {
        synchronized (state) {
            return state.get();
        }
    }

    private synchronized void setState(int state) {
        synchronized (this.state) {
            this.state.set(state);
        }
    }



    public void start() throws IOException {
        if (getState() != STATE_CREATED) {
            throw new IllegalStateException("Already started");
        } else{
            setState(STATE_SEARCH_CATEGORIES);

            parseStartPageForCategories();

            setState(STATE_SEARCH_BOOKS);

            synchronized (exec) {
                for (int i = 0; i < (nThreads); i++) {
                    exec.execute(
                        new CategoryItemProcessor(urlHost,
                            () -> {
                                synchronized (categoryItems) {
                                    return categoryItems.isEmpty() ? null : categoryItems.removeFirst();
                                }
                            }, (refs) -> {
                                synchronized (foundBookRefs) {
                                    foundBookRefs.addAll(refs);
                                }
                            }
                        )
                    );
                }
            }
        }
    }

    public void join(long timeout, @NonNull TimeUnit units) throws InterruptedException {
        if (getState() == STATE_CREATED) {
            throw new IllegalStateException("Not started yet");
        } else {
            synchronized (exec) {
                exec.shutdown();
                exec.awaitTermination(timeout, units);
                setState(STATE_COMPLETED);
                if (!exec.isTerminated()) throw new InterruptedException("timeout");
            }
        }
    }


    public Collection<BookRefItem> getDecodedBookReferences() {
        if (getState() == STATE_COMPLETED) {
            List<BookRefItem> ret;
            synchronized (foundBookRefs) {
                ret = new ArrayList<>(foundBookRefs.size());
                ret.addAll(foundBookRefs);
            }
            return ret;
        } else {
            throw new IllegalStateException("Not completed yet");
        }
    }




    public static final String INITIAL_CONTENT_TAG = "<div class=\"content clearfix category index\">";

    private void parseStartPageForCategories() throws IOException {
        Log.d(Thread.currentThread().getName(), "--> Start loading initial page - "+urlStartPage);
        String startPage = new HtmlPageLoader(urlStartPage).load(1000000);
        Log.d(Thread.currentThread().getName(), "<-- Start page has been loaded. Size="+startPage.length());

        int index = startPage.indexOf(INITIAL_CONTENT_TAG);
        if (index < 0) {
            Log.d(Thread.currentThread().getName(), "Bad page content. The %s is not found.", INITIAL_CONTENT_TAG);
            return;
        }


        String content = new XmlTagExtractor(startPage).getTag("div", index);
        if (content == null) {
            Log.d(Thread.currentThread().getName(), "Error extract %s content from the initial HTML page", INITIAL_CONTENT_TAG);
            return;
        } else {
            Log.d(Thread.currentThread().getName(), "HTML content extracted. Size = "+content.length());
        }


        JSONObject jPage;
        try {
            jPage = org.json.XML.toJSONObject(content, true);
            Log.d(Thread.currentThread().getName(), "HTML content was successfully converted to JSON");
        } catch (JSONException e) {
            Log.d(Thread.currentThread().getName(), "cannot convert HTML content to JSON - "+e.getMessage());
            return;
        }

        CategoryIndexParser itemsParser = new CategoryIndexParser(jPage, urlHost.toString());

        synchronized (categoryItems) {
            categoryItems.addAll(itemsParser.parse());
        }
    }


}
