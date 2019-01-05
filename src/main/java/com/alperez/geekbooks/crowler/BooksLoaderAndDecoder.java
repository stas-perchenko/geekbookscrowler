package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.data.BookModel;
import com.alperez.geekbooks.crowler.data.BookRefItem;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BooksLoaderAndDecoder {


    private final ExecutorService exec;
    final int nThreads;
    private boolean isStarted;
    final Iterator<BookRefItem> src;

    private final List<BookModel> result = new LinkedList<>();

    public BooksLoaderAndDecoder(Collection<BookRefItem> bookRefs, int nThreads) {
        exec = Executors.newFixedThreadPool(this.nThreads = nThreads);
        this.src = bookRefs.iterator();
    }

    public synchronized boolean isStarted() {
        return isStarted;
    }

    public synchronized void start() {
        if (isStarted) {
            throw new IllegalStateException("Already started");
        }

        for (int i=0; i<nThreads; i++) {
            exec.execute(new BookItemProcessor(() -> {
                        synchronized (src) {
                            return src.hasNext() ? src.next() : null;
                        }
                    },
                    book -> addDecodedBook(book)
            ));
        }

        isStarted = true;
    }

    public synchronized void join() throws InterruptedException {
        if (!isStarted) throw new IllegalStateException("Not started yet");
        exec.shutdown();
        exec.awaitTermination(45, TimeUnit.MINUTES);
        if (!exec.isTerminated()) throw new InterruptedException("timeout");
    }




    private void addDecodedBook(BookModel book) {
        synchronized (result) {
            for (BookModel b : result) {
                if (b.id() == book.id()) return;
            }
            result.add(book);
        }
    }

    public Collection<BookModel> getDecodedBooks() {
        List<BookModel> ret;
        synchronized (result) {
            ret = new ArrayList<>(result.size());
            ret.addAll(result);
        }
        return ret;
    }
}
