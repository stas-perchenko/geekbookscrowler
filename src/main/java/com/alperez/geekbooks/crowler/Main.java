package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.utils.HtmlPageLoader;
import com.alperez.geekbooks.crowler.utils.Log;
import com.alperez.geekbooks.crowler.utils.TagExtractor;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static final String INITIAL_CONTENT_TAG = "<div class=\"content clearfix category index\">";

    public static void main(String[] args) {
        try {
            Log.d(Thread.currentThread().getName(), "main() was started: %s", Arrays.toString(args));
            Main main = new Main(new URL(args[0]), (args.length > 1) ? Integer.parseInt(args[1]) : 1);
            main.start();
            main.join();

            // The set of BookRefItem is filled in here

            //TODO Implement further !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            main.printAllFoundBookReferences();


        } catch (MalformedURLException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        Log.d(Thread.currentThread().getName(), "main() has been finished");
    }

    public Main(URL urlStartPage, int nThreads) throws MalformedURLException {
        this.urlStartPage = urlStartPage;
        this.urlHost = new URL(String.format("%s://%s", urlStartPage.getProtocol(), urlStartPage.getHost()));
        exec = Executors.newFixedThreadPool(this.nThreads = nThreads);
    }

    private final int nThreads;
    private final URL urlStartPage;
    private final URL urlHost;
    private final ExecutorService exec;

    private final Deque<CategoryItem> categoryItems = new LinkedList<>();
    private final Set<BookRefItem> foundBookRefs = new HashSet<>();

    public void printAllFoundBookReferences() {
        System.out.println(String.format("\n\n===================  Found totally %d book references  ==================", foundBookRefs.size()));
        for (BookRefItem ref : foundBookRefs) {
            System.out.println("\t"+ref+";");
        }
    }

    public void start() {
        exec.execute(this::parseStartPageForCategories);
        for (int i=0; i<(nThreads - 0); i++) {
            exec.execute(new CategoryProcessor(categoryItems, urlHost, (refs) -> {
                synchronized (foundBookRefs) {
                    foundBookRefs.addAll(refs);
                }
            }));
        }
    }

    public void join() throws InterruptedException {
        exec.shutdown();
        exec.awaitTermination(45, TimeUnit.MINUTES);
    }


    private void parseStartPageForCategories() {
        long tStart = System.currentTimeMillis();
        try {
            Log.d(Thread.currentThread().getName(), "--> Start loading initial page - "+urlStartPage);
            String startPage = new HtmlPageLoader(urlStartPage).load(1000000);
            Log.d(Thread.currentThread().getName(), "<-- Start page has been loaded. Size="+startPage.length());

            int index = startPage.indexOf(INITIAL_CONTENT_TAG);
            if (index < 0) {
                Log.d(Thread.currentThread().getName(), "Bad page content. The %s is not found.", INITIAL_CONTENT_TAG);
                return;
            }


            String content = new TagExtractor(startPage).getTag("div", index);
            if (content == null) {
                Log.d(Thread.currentThread().getName(), "Error extract %s content from the initial HTML page", INITIAL_CONTENT_TAG);
                return;
            } else {
                Log.d(Thread.currentThread().getName(), "HTML content extracted. Size = "+content.length());
            }


            JSONObject jPage = null;
            try {
                jPage = org.json.XML.toJSONObject(content, true);
                Log.d(Thread.currentThread().getName(), "HTML content was successfully converted to JSON");
            } catch (JSONException e) {
                Log.d(Thread.currentThread().getName(), "cannot convert HTML content to JSON - "+e.getMessage());
                return;
            }

            Collection<CategoryItem> items = new CategoryIndexParser(jPage, urlHost.toString()).parse();

            ensureSpentTime(tStart, 300);

            synchronized (categoryItems) {
                categoryItems.addAll(items);
                categoryItems.notifyAll();
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            ensureSpentTime(tStart, 300);
            synchronized (categoryItems) {
                categoryItems.notifyAll();
            }
        }
    }

    private void ensureSpentTime(long tStart, int needSpend) {
        int dt = (int)(System.currentTimeMillis() - tStart);
        if (dt < needSpend) {
            try {
                Thread.sleep(needSpend - dt);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

}
