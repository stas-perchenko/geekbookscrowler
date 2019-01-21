package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.data.BookModel;
import com.alperez.geekbooks.crowler.data.BookRefItem;
import com.alperez.geekbooks.crowler.utils.FileUtils;
import com.alperez.geekbooks.crowler.utils.Log;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {

    private static URL argUrl;
    private static Integer argNThreads;
    private static String argDestBooksFolder;
    private static String argDestDbName;


    private static void extractArguments(String[] args) throws MalformedURLException {
        for (String arg : args) {
            int delimIndex = arg.indexOf('=');
            if (delimIndex <= 0) throw new IllegalArgumentException("Wrong argument - "+arg);
            String value = arg.substring(delimIndex + 1);
            switch (arg.substring(0, delimIndex)) {
                case "url":
                    argUrl = new URL(value);
                    break;
                case "nThreads":
                    argNThreads = Integer.parseInt(value);
                    break;
                case "dstBooks":
                    if (value.length() == 0) throw new IllegalArgumentException("Missing argument value - dstBooks");
                    argDestBooksFolder = value;
                    break;
                case "dbName":
                    if (value.length() == 0) throw new IllegalArgumentException("Missing argument value - dbName");
                    argDestDbName = value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument - " + arg.substring(0, delimIndex));
            }

            if (argNThreads == null) argNThreads = 1;

            if (argUrl == null) {
                throw new IllegalArgumentException("Missing argument - url");
            } else if (argDestBooksFolder == null) {
                throw new IllegalArgumentException("Missing argument - dstBooks");
            } else if (argDestDbName == null) {
                throw new IllegalArgumentException("Missing argument - dbName");
            }
        }
    }

    public static void main(String[] args) {
        try {
            Log.d(Thread.currentThread().getName(), "main() was started: %s", Arrays.toString(args));
            extractArguments(args);

            CategoriesLoaderAndDecoder booksSearcher = new CategoriesLoaderAndDecoder(argUrl, argNThreads);
            booksSearcher.start();
            booksSearcher.join(45, TimeUnit.MINUTES);


            Collection<BookRefItem> refs = booksSearcher.getDecodedBookReferences();
            printAllFoundBookReferences(refs);


            List<BookRefItem> dbgRefs = new ArrayList<>(300);
            for (int i=0; i<300; i++) dbgRefs.add(  (BookRefItem) ((List) refs).get(i)  );


            URL host = new URL(String.format("%s://%s", argUrl.getProtocol(), argUrl.getHost()));
            BooksLoaderAndDecoder booksDecoder = new BooksLoaderAndDecoder(host, dbgRefs, argNThreads);
            booksDecoder.start();
            booksDecoder.join(45, TimeUnit.MINUTES);
            Collection<BookModel> books = booksDecoder.getDecodedBooks();



            File destinationDir = new File(argDestBooksFolder);
            FileUtils.createDirIfNeeded(destinationDir);
            FileUtils.clearFolder(destinationDir);
            PdfFinder.forBooks(books).toFolder(destinationDir).findAndCopy();




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
