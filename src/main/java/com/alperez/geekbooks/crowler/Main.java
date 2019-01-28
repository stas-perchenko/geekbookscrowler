package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.data.dbmodel.BookModel;
import com.alperez.geekbooks.crowler.data.parsing.BookRefItem;
import com.alperez.geekbooks.crowler.storage.BookDbSaver;
import com.alperez.geekbooks.crowler.utils.FileUtils;
import com.alperez.geekbooks.crowler.utils.Log;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    private static URL argUrl;
    private static Integer argNThreads;
    private static String argDestBooksFolder;
    private static String argDestDbName;
    private static Integer argMaxBook;

    public static void main(String[] args) {
        try {
            Log.d(Thread.currentThread().getName(), "main() was started: %s", Arrays.toString(args));
            extractArguments(args);

            CategoriesLoaderAndDecoder booksSearcher = new CategoriesLoaderAndDecoder(argUrl, argNThreads);
            booksSearcher.start();
            booksSearcher.join(45, TimeUnit.MINUTES);


            Collection<BookRefItem> refs = booksSearcher.getDecodedBookReferences();
            printAllFoundBookReferences(refs);





            URL host = new URL(String.format("%s://%s", argUrl.getProtocol(), argUrl.getHost()));
            BooksLoaderAndDecoder booksDecoder = new BooksLoaderAndDecoder(host, (argMaxBook == null) ? refs : limitBooks(refs, argMaxBook), argNThreads);
            booksDecoder.start();
            booksDecoder.join(45, TimeUnit.MINUTES);
            Collection<BookModel> books = booksDecoder.getDecodedBooks();


            //----  Find related PDF files for books  ----
            File destinationDir = new File(argDestBooksFolder);
            FileUtils.createDirIfNeeded(destinationDir);
            FileUtils.clearFolder(destinationDir);
            PdfFinder.forBooks(books).toFolder(destinationDir).findAndCopy();


            //----  Save result to database  ----
            // load the sqlite-JDBC driver using the current class loader
            System.out.println(String.format("\n\n===============================  Start saving decoded Books into the local DB - %s  =====================================", argDestDbName));
            Class.forName("org.sqlite.JDBC");
            BookDbSaver saver = new BookDbSaver(argDestDbName);
            //saver.dropAllTables();
            //saver.createTables();
            saver.initAllTables();
            for (BookModel b : books) {
                try {
                    saver.insertBook(b);
                } catch (SQLException e) {
                    //TODO Log this properly
                    e.printStackTrace(System.out);
                }
            }


            //----  Select and Log-out all books for test purpose  ----
            //TODO Implement further !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        } catch (SQLException e) {
            Log.d("MAIN", "<~~~  Top-level SQLException - "+e.getMessage());
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        } catch (Exception e) {
            Log.d("MAIN", "<~~~  Top-level Exception - "+e.getMessage());
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }
        Log.d(Thread.currentThread().getName(), "main() has been finished");
    }




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
                case "maxBooks":
                    argMaxBook = Integer.parseInt(value);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument - " + arg.substring(0, delimIndex));
            }
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




    public static void printAllFoundBookReferences(Collection<BookRefItem> foundBookRefs) {
        System.out.println(String.format("\n\n===================  Found totally %d book references  ==================", foundBookRefs.size()));
        for (BookRefItem ref : foundBookRefs) {
            System.out.println("\t"+ref+";");
        }
    }


    private static Collection<BookRefItem> limitBooks(Collection<BookRefItem> refs, int max) {
        int finZise = Math.min(refs.size(), max);
        List<BookRefItem> result = new ArrayList<>(finZise);
        for (int i=0; i<finZise; i++) {
            result.add(  (BookRefItem) ((List) refs).get(i)  );
        }
        return result;
    }


}
