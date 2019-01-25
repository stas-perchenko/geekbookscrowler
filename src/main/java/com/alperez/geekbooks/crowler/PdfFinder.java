package com.alperez.geekbooks.crowler;

import com.alperez.geekbooks.crowler.data.BookModel;
import com.alperez.geekbooks.crowler.data.LongId;
import com.alperez.geekbooks.crowler.utils.FileUtils;
import com.alperez.geekbooks.crowler.utils.Log;
import com.alperez.geekbooks.crowler.utils.NonNull;

import java.io.File;
import java.util.*;

public final class PdfFinder {


    public static PdfFinder forBooks(Collection<BookModel> books) {
        return new PdfFinder(books);
    }

    private final List<BookModel> books;
    private File destinationDir;

    private PdfFinder(Collection<BookModel> books) {
        this.books = new ArrayList<>(books.size());
        this.books.addAll(books);
    }

    public PdfFinder toFolder(@NonNull File dst) {
        destinationDir = dst;
        return this;
    }

    public Map<LongId<BookModel>, String> findAndCopy() {
        if(destinationDir == null) throw new IllegalStateException("The toFolder() was not called");

        Map<String, String[]> mapFoldersFiles = new HashMap<>(100);
        Set<String> setNonExistedFolders = new HashSet<>(100);


        Map<LongId<BookModel>, String> result = new HashMap<>();
        for (BookModel b : books) {
            Log.d("\r\n\r\n\r\nBOOK", "--->  Evaluate book - "+b.title());
            Log.d("BOOK_CATEGORY", b.category().toString());
            Log.d("PDF_PATH", b.origPdfPath().toString());

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
                String hash = FileUtils.copyFileWithHash(fSrc, fTmp);

                File fDst = new File(destinationDir, hash);
                Log.d("COPY", fSrc.getAbsolutePath()+"   to   "+fDst.getAbsolutePath());
                FileUtils.copyFileToFile(fTmp, fDst);
                result.put(b.id(), hash);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        } //for()
        return result;
    }



    private String getFolder(BookModel book) {
        return book.category().toString().toLowerCase().replace("->", File.separator);
    }

    private String getFileName(BookModel book) {
        String path = book.origPdfPath().getPath();
        return path.substring(path.lastIndexOf('/')+1);
    }

    private File selectFromMultipleOptions(BookModel book, String folder, List<String> fNames) {
        //TODO Implement this !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        Log.d("!!!!!", "!!!!!!!   Multiple options  !!!!!!!!!!!");
        return null;
    }
}
