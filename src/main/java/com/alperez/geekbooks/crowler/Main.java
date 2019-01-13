package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.data.BookModel;
import com.alperez.geekbooks.crowler.data.BookRefItem;
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


            List<BookRefItem> dbgRefs = new ArrayList<>(300);
            for (int i=0; i<300; i++) dbgRefs.add(  (BookRefItem) ((List) refs).get(i)  );


            URL host = new URL(String.format("%s://%s", urlStartPage.getProtocol(), urlStartPage.getHost()));
            BooksLoaderAndDecoder booksDecoder = new BooksLoaderAndDecoder(host, dbgRefs, nThreads);
            booksDecoder.start();
            booksDecoder.join(45, TimeUnit.MINUTES);
            Collection<BookModel> books = booksDecoder.getDecodedBooks();


            Map<String, String[]> mapFoldersFiles = new HashMap<>(100);
            Set<String> setNonExistedFolders = new HashSet<>(100);

            File destinationDir = new File("files");
            createDirIfNeeded(destinationDir);
            clearFolder(destinationDir);

            for (BookModel b : books) {
                Log.d("\r\n\r\n\r\nBOOK", "--->  Evaluate book - "+b.title());
                Log.d("BOOK_CATEGORY", b.category().toString());
                Log.d("PDF_PATH", b.pdfPath().toString());

                String folder = getFolder(b);
                String fName  = getFileName(b);
                Log.d("EXTRACT", "folder - %s, f_name - %s", folder, fName);


                String[] optNames = mapFoldersFiles.get(folder);
                if (optNames == null) {
                    if (setNonExistedFolders.contains(folder)) {
                        Log.d("ERROR", "<~~~ Folder does not exist - "+folder);
                        continue;
                    }
                    optNames = (new File(folder)).list((file, s) -> s.toLowerCase().endsWith(".pdf"));
                    if (optNames == null) {
                        Log.d("ERROR", "<~~~ Folder does not exist - "+folder);
                        continue;
                    } else {
                        Log.d("OPTIONS", "<--- Add new folder %s -> %s", folder, Arrays.toString(optNames));
                        mapFoldersFiles.put(folder, optNames);
                    }
                }

                List<String> foundOpts = new ArrayList<>(1);
                String fn = fName.substring(0, fName.lastIndexOf('.'));
                for (String opt : optNames) {
                    if (opt.startsWith(fn)) foundOpts.add(opt);
                }

                Log.d("TEST_FILE", "Testing file %s in folder %s -> %s", fName, folder, foundOpts);



                File fSrc;
                if (foundOpts.size() == 0) {
                    Log.d("NOT_FOUND", "Required file is not found in folder - %s", folder);
                    continue;
                } else if (foundOpts.size() == 1) {
                    fSrc = new File(folder, foundOpts.get(0));
                    if (fSrc.exists()) {
                        float sz = (float) ((double)fSrc.length() / (1024*1024));
                        float delta = Math.abs(sz - b.pdfSize());
                        Log.d("SIZE", "File size delta - "+delta);
                    } else {
                        Log.d("ERROR", "File not exists - "+fSrc.getAbsolutePath());
                    }
                } else {
                    fSrc = selectFromMultipleOptions(b, folder, foundOpts);
                }

                if (fSrc == null) {
                    Log.d("NOT_FOUND", "Required file is not found in folder - %s", folder);
                    continue;
                }


                try {
                    File fTmp = new File("work.file");
                    if (fTmp.exists()) fTmp.delete();
                    String hash = copyFileWithHash(fSrc, fTmp);

                    File fDst = new File(destinationDir, hash);
                    Log.d("COPY", fSrc.getAbsolutePath()+"   to   "+fDst.getAbsolutePath());
                    copyFileToFile(fTmp, fDst);
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            } //for()


            //TODO Implement further !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }
        Log.d(Thread.currentThread().getName(), "main() has been finished");
    }

    private static String getFolder(BookModel book) {
        return book.category().toString().toLowerCase().replace("->", File.separator);
    }

    private static String getFileName(BookModel book) {
        String path = book.pdfPath().getPath();
        return path.substring(path.lastIndexOf('/')+1);
    }

    private static File selectFromMultipleOptions(BookModel book, String folder, List<String> fNames) {
        Log.d("!!!!!", "!!!!!!!   Multiple options  !!!!!!!!!!!");
        return null;
    }


    /**
     *
     * @param sourceFile
     * @param destFile
     * @return Number of bytes copied
     * @throws IOException
     */
    public static long copyFileToFile(File sourceFile, File destFile) throws IOException {
        if (sourceFile == null) return 0;
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(sourceFile);
            fos = new FileOutputStream(destFile);
            source = fis.getChannel();
            destination = fos.getChannel();
            long count = 0;
            long size = source.size();
            while((count += destination.transferFrom(source, count, size - count)) < size);
            return count;
        } finally {
            if(source != null) {
                source.close();
            }
            if (fis != null) {
                fis.close();
            }
            if(destination != null) {
                destination.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    private static String copyFileWithHash(File src, File dst) throws NoSuchAlgorithmException, IOException {
        try (FileInputStream fIs = new FileInputStream(src); FileOutputStream fOs = new FileOutputStream(dst)) {
            InputStream is = new BufferedInputStream(fIs);
            OutputStream os = new BufferedOutputStream(fOs);

            byte data[] = new byte[8192];
            int count;

            MessageDigest digester = MessageDigest.getInstance("SHA-256");
            while ((count = is.read(data)) != -1) {
                os.write(data, 0, count);
                digester.update(data, 0, count);
            }

            os.flush();
            return new String(encodeBytesToHex(digester.digest()));
        }
    }

    private static char[] encodeBytesToHex(byte[] data) {
        char[] ret = new char[data.length * 2];
        for (int i=0, j=0; i<data.length; i++, j+=2) {
            int c = (char)((data[i] >> 4) & 0x0F);
            if (c > 9) c += 7;
            ret[j] = (char)(c+0x30);


            c = (char)(data[i] & 0x0F);
            if (c > 9) c += 7;
            ret[j+1] = (char)(c + 0x30);

        }
        return ret;
    }

    /**
     * Creates a directory and puts a .nomedia file in it
     *
     * @param dir
     * @return new dir
     * @throws IOException
     */
    private static File createDirIfNeeded(File dir) throws IOException {
        if ((dir != null) && !dir.exists()) {
            if (!dir.mkdirs() && !dir.isDirectory()) {
                throw new IOException("error create directory");
            }
            File noMediaFile = new File(dir, ".nomedia");
            noMediaFile.createNewFile();
        }
        return dir;
    }

    public static int clearFolder(File folder) {
        int numDeleted = 0;
        if ((folder == null) || !folder.isDirectory()) {
            return numDeleted;
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    numDeleted += clearFolder(f);
                } else {
                    if (f.delete()) {
                        numDeleted ++;
                    }
                }
            }
        }
        return numDeleted;
    }






    public static void printAllFoundBookReferences(Collection<BookRefItem> foundBookRefs) {
        System.out.println(String.format("\n\n===================  Found totally %d book references  ==================", foundBookRefs.size()));
        for (BookRefItem ref : foundBookRefs) {
            System.out.println("\t"+ref+";");
        }
    }





}
