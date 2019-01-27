package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.data.dbmodel.BookModel;
import com.alperez.geekbooks.crowler.data.parsing.BookRefItem;
import com.alperez.geekbooks.crowler.data.LongId;
import com.alperez.geekbooks.crowler.utils.Log;
import com.alperez.geekbooks.crowler.utils.NonNull;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BooksLoaderAndDecoder {
    public static final int STATE_CREATED = 0;
    public static final int STATE_STARTED = 1;
    public static final int STATE_LOADED = 2;
    public static final int STATE_COMPLETED = 3;

    public static final String TAG_RELATIONS = "RELATIONS";

    //--- Source and parameters ---
    private final URL urlHost;
    private final int nThreads;
    final Iterator<BookRefItem> src;

    //--- Workers and state ---
    private final AtomicInteger state = new AtomicInteger(STATE_CREATED);
    private final AtomicInteger nWorkingThreads = new AtomicInteger(0);
    private final ExecutorService exec;

    //--- Result ---
    private final List<BookModel> result = new LinkedList<>();
    private final List<Map<String, Object>> relations = new LinkedList<>();

    public BooksLoaderAndDecoder(@NonNull URL urlHost, Collection<BookRefItem> bookRefs, int nThreads) {
        this.urlHost = urlHost;
        exec = Executors.newFixedThreadPool(this.nThreads = nThreads);
        this.src = bookRefs.iterator();
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

    public void start() {
        if (getState() == STATE_CREATED) {
            setState(STATE_STARTED);
        } else {
            throw new IllegalStateException("Already started");
        }

        synchronized (exec) {
            for (int i=0; i<nThreads; i++) {
                exec.execute(
                    new BookItemProcessor(
                            urlHost,
                            () -> {
                                synchronized (src) {
                                    return src.hasNext() ? src.next() : null;
                                }
                            },
                            (book, relatedBooks) -> addDecodedBook(book, relatedBooks),
                            () -> { // Thread complete listener
                                if (nWorkingThreads.decrementAndGet() == 0) {
                                    setState(STATE_LOADED);
                                    Log.d(TAG_RELATIONS, "---> Start evaluate relations at %1$tT.%1$tL", new Date());
                                    resolveBookRelations();
                                    Log.d(TAG_RELATIONS, "<--- End evaluate relations at %1$tT.%1$tL", new Date());
                                    setState(STATE_COMPLETED);
                                }
                            }
                    )
                );
                nWorkingThreads.incrementAndGet();
            }
        }
    }

    public void join(long timeout, @NonNull TimeUnit units) throws InterruptedException {
        if (getState() != STATE_STARTED) {
            throw new IllegalStateException("Wrong state - " + getState());
        } else {
            synchronized (exec) {
                exec.shutdown();
                exec.awaitTermination(timeout, units);
                if (!exec.isTerminated()) throw new InterruptedException("timeout");
            }
        }
    }




    private void addDecodedBook(BookModel book, List<URL> related) {
        synchronized (result) {
            for (BookModel b : result) {
                if (b.id() == book.id()) return;
            }
            result.add(book);
            Map<String, Object> relItem = new HashMap<>();
            relItem.put("book", book);
            relItem.put("related", related);
            relations.add(relItem);
        }
    }

    private void resolveBookRelations() {
        int state = getState();
        if (state == STATE_COMPLETED) {
            return; //Already done
        } else if (state < STATE_LOADED) {
            throw new IllegalStateException("Not loaded yet");
        }

        synchronized (result) {
            List<BookModel> updatedResult = new ArrayList<>(result.size());
            for (BookModel book : result) {
                List<LongId<BookModel>> relatedIds = new ArrayList<>(10);

                for (Iterator<Map<String, Object>> itr = relations.iterator(); itr.hasNext(); ) {
                    Map<String, Object> relItem = itr.next();
                    if (((BookModel) relItem.get("book")).id() == book.id()) {
                        itr.remove();
                        List<URL> relatedURLs = (List<URL>) relItem.get("related");
                        mapBookURLsToIds(relatedURLs, relatedIds);
                        break;
                    }
                }
                updatedResult.add(book.withRelatedBookIds(relatedIds));

                Log.d(TAG_RELATIONS, "\t<--- processed \"%s\". relations - %s", book.title(), relatedIds);
            }
            result.clear();
            result.addAll(updatedResult);
        }
    }

    private void mapBookURLsToIds(@NonNull List<URL> relatedURLs, @NonNull List<LongId<BookModel>> relatedIds) {
        synchronized (result) {
            for (URL u : relatedURLs) {
                for (BookModel book : result) {
                    if(book.geekBooksAddress().equals(u)) {
                        relatedIds.add(book.id());
                    }
                }
            }
        }
    }




    public Collection<BookModel> getDecodedBooks() {
        if (getState() == STATE_COMPLETED) {
            List<BookModel> ret;
            synchronized (result) {
                ret = new ArrayList<>(result.size());
                ret.addAll(result);
            }
            return ret;
        } else {
            throw new IllegalStateException("Not completed yet");
        }
    }
}
