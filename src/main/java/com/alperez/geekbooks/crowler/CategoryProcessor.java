package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.utils.Log;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class CategoryProcessor implements Runnable {
    private static final AtomicInteger static_inst_cntr = new AtomicInteger(0);


    private final int nInstance;
    private final Collection<CategoryItem> categoryItems;

    private final String TAG;

    public CategoryProcessor(Collection<CategoryItem> categoryItems) {
        this.categoryItems = categoryItems;
        nInstance = static_inst_cntr.incrementAndGet();
        TAG = String.format("%s-%d", getClass().getSimpleName(), nInstance);
    }

    @Override
    public void run() {
        Log.d(TAG, "---> run() start");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "<--- run() end");
    }
}
