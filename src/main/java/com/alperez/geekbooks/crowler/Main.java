package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.data.BookModel;
import com.alperez.geekbooks.crowler.data.BookRefItem;
import com.alperez.geekbooks.crowler.utils.Log;

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

            /*Main main = new Main(urlStartPage, nThreads);
            main.start();
            main.join();

            // The set of BookRefItem is filled in here


            main.printAllFoundBookReferences();*/

            URL host = new URL(String.format("%s://%s", urlStartPage.getProtocol(), urlStartPage.getHost()));
            BooksLoaderAndDecoder booksDecoder = new BooksLoaderAndDecoder(host, main.foundBookRefs, nThreads);
            booksDecoder.start();
            booksDecoder.join(45, TimeUnit.MINUTES);
            Collection<BookModel> books = booksDecoder.getDecodedBooks();

            //TODO Implement further !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        } catch (MalformedURLException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        Log.d(Thread.currentThread().getName(), "main() has been finished");
    }







    public void printAllFoundBookReferences() {
        System.out.println(String.format("\n\n===================  Found totally %d book references  ==================", foundBookRefs.size()));
        for (BookRefItem ref : foundBookRefs) {
            System.out.println("\t"+ref+";");
        }
    }





}
