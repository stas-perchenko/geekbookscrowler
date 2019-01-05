package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.data.BookModel;
import com.alperez.geekbooks.crowler.data.BookRefItem;
import com.alperez.geekbooks.crowler.utils.Log;
import com.alperez.geekbooks.crowler.utils.NonNull;
import com.alperez.geekbooks.crowler.utils.Nullable;

import java.util.Random;

public class BookItemProcessor implements Runnable {


    public interface BookRefItemProvider {
        @Nullable BookRefItem getBookRefItem();
    }

    public interface OnBookDecodeListener {
        void onBookDecoded(@NonNull BookModel book);
    }


    private final BookRefItemProvider src;
    private final OnBookDecodeListener dst;

    public BookItemProcessor(@NonNull BookRefItemProvider src, @NonNull OnBookDecodeListener dst) {
        this.src = src;
        this.dst = dst;
    }

    @Override
    public void run() {
        BookRefItem ref;
        while((ref = src.getBookRefItem()) != null) {
            BookModel bm = evaluateBookReference(ref);
            if (bm != null) dst.onBookDecoded(bm);
        }
    }

    private Random rnd = new Random();

    @Nullable
    private BookModel evaluateBookReference(BookRefItem ref) {
        //TODO Implement book loading and decode logic here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        Log.d(Thread.currentThread().getName(), "---> Start evaluating book: %s", ref);

        try {
            Thread.sleep(rnd.nextInt(150) + 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
