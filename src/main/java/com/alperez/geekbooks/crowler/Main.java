package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.data.BookModel;
import com.alperez.geekbooks.crowler.data.BookRefItem;
import com.alperez.geekbooks.crowler.utils.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class Main {



    public static void main(String[] args) {
        try {
            Log.d(Thread.currentThread().getName(), "main() was started: %s", Arrays.toString(args));
            URL urlStartPage = new URL(args[0]);
            int nThreads = (args.length > 1) ? Integer.parseInt(args[1]) : 1;

            CategoriesLoaderAndDecoder booksSearcher = new CategoriesLoaderAndDecoder(urlStartPage, nThreads);
            booksSearcher.start();
            booksSearcher.join(45, TimeUnit.MINUTES);


            Collection<BookRefItem> refs = booksSearcher.getDecodedBookReferences();

            printAllFoundBookReferences(refs);

            URL host = new URL(String.format("%s://%s", urlStartPage.getProtocol(), urlStartPage.getHost()));
            BooksLoaderAndDecoder booksDecoder = new BooksLoaderAndDecoder(host, refs, nThreads);
            booksDecoder.start();
            booksDecoder.join(45, TimeUnit.MINUTES);
            Collection<BookModel> books = booksDecoder.getDecodedBooks();

            //TODO Implement further !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }
        Log.d(Thread.currentThread().getName(), "main() has been finished");
    }







    public static void printAllFoundBookReferences(Collection<BookRefItem> foundBookRefs) {
        System.out.println(String.format("\n\n===================  Found totally %d book references  ==================", foundBookRefs.size()));
        for (BookRefItem ref : foundBookRefs) {
            System.out.println("\t"+ref+";");
        }
    }





}
