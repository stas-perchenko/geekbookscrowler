package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.utils.HtmlPageLoader;
import com.alperez.geekbooks.crowler.utils.Log;
import com.alperez.geekbooks.crowler.utils.TagExtractor;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static final String INITIAL_CONTENT_TAG = "<div class=\"content clearfix category index\">";

    public static void main(String[] args) {
        try {

            Main main = new Main(new URL(args[0]), (args.length > 1) ? Integer.parseInt(args[1]) : 1);
            main.start();
            main.join();
        } catch (MalformedURLException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Main(URL urlStartPage, int nThreads) throws MalformedURLException {
        this.urlStartPage = urlStartPage;
        exec = Executors.newFixedThreadPool(this.nThreads = nThreads);
    }

    private final int nThreads;
    private final URL urlStartPage;
    private final ExecutorService exec;

    private final Collection<CategoryItem> categoryItems = new ArrayList<>(100);



    public void start() {
        exec.execute(this::parseStartPageForCategories);
        for (int i=0; i<nThreads; i++) {
            exec.execute(new CategoryProcessor(categoryItems));
        }
    }

    public void join() throws InterruptedException {
        exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
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

            //TODO Implement further !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

            System.out.println(jPage);


        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            int dt = (int)(System.currentTimeMillis() - tStart);
            if (dt < 300) {
                try {
                    Thread.sleep(300 - dt);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            synchronized (categoryItems) {
                categoryItems.notifyAll();
            }
        }

    }

}
