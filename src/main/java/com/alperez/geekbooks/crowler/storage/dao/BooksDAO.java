package com.alperez.geekbooks.crowler.storage.dao;

import com.alperez.geekbooks.crowler.data.dbmodel.BookModel;
import com.alperez.geekbooks.crowler.storage.DbTableManager;
import com.alperez.geekbooks.crowler.storage.executor.ContentValue;
import com.alperez.geekbooks.crowler.storage.executor.DbExecutor;

import java.sql.Connection;
import java.sql.SQLException;

public class BooksDAO implements DbTableManager {
    private static final String TABLE_NAME = "Books";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_GEEK_BOOKS_ADRESS = "geekBooks_address";
    private static final String COLUMN_IMAGE_PATH = "img_path";
    private static final String COLUMN_IMAGE_WIDTH = "img_width";
    private static final String COLUMN_IMAGE_HEIGHT = "img_height";
    private static final String COLUMN_PDF_ORIGINAL_PATH = "pdf_orig_path";
    private static final String COLUMN_PDF_SIZE = "pdf_size";
    private static final String COLUMN_PDF_FINAL_FILE_NAME = "pdf_fin_fname";
    private static final String COLUMN_ISBN = "isbn";
    private static final String COLUMN_ASIN = "asin";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_SUBTITLE = "subtitle";
    private static final String COLUMN_YEAR = "year";
    private static final String COLUMN_N_PAGES = "n_pages";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_CATEGORY_ID = "category_id";




    private final DbExecutor executor;

    public BooksDAO(Connection connection) {
        executor = new DbExecutor(connection);
    }

    @Override
    public void createTable() throws SQLException {
        String sql = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT, %s INTEGER, %s INTEGER, %s TEXT, %s REAL, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s INTEGER, %s INTEGER, %s TEXT, %s INTEGER);",
                TABLE_NAME,
                COLUMN_ID,
                COLUMN_GEEK_BOOKS_ADRESS,
                COLUMN_IMAGE_PATH,
                COLUMN_IMAGE_WIDTH,
                COLUMN_IMAGE_HEIGHT,
                COLUMN_PDF_ORIGINAL_PATH,
                COLUMN_PDF_SIZE,
                COLUMN_PDF_FINAL_FILE_NAME,
                COLUMN_ISBN,
                COLUMN_ASIN,
                COLUMN_TITLE,
                COLUMN_SUBTITLE,
                COLUMN_YEAR,
                COLUMN_N_PAGES,
                COLUMN_DESCRIPTION,
                COLUMN_CATEGORY_ID);
        executor.execUpdateNumAffected(sql);
    }

    @Override
    public void dropTable() throws SQLException {
        executor.execUpdateNumAffected(String.format("drop table %s;", TABLE_NAME));
    }

    @Override
    public boolean isTableExist() throws SQLException {
        return executor.isTableExist(TABLE_NAME);
    }

    public void createOrUpdateBook(BookModel book) throws SQLException {
        ContentValue cv = new ContentValue();
        cv.put(COLUMN_GEEK_BOOKS_ADRESS, book.geekBooksAddress());
        if (book.imagePath() != null)
            cv.put(COLUMN_IMAGE_PATH, book.imagePath());
        if (book.imageDimensions() != null) {
            cv.put(COLUMN_IMAGE_WIDTH, book.imageDimensions().getWidth());
            cv.put(COLUMN_IMAGE_HEIGHT, book.imageDimensions().getHeight());
        }
        cv.put(COLUMN_PDF_ORIGINAL_PATH, book.origPdfPath());
        cv.put(COLUMN_PDF_SIZE, book.pdfSize());
        if (book.finPdfFileName() != null)
            cv.put(COLUMN_PDF_FINAL_FILE_NAME, book.finPdfFileName());
        if (book.isbn() != null)
            cv.put(COLUMN_ISBN, book.isbn());
        if (book.asin() != null)
            cv.put(COLUMN_ASIN, book.asin());
        cv.put(COLUMN_TITLE, book.title());
        if (book.subtitle() != null)
            cv.put(COLUMN_SUBTITLE, book.subtitle());
        cv.put(COLUMN_YEAR, book.year());
        cv.put(COLUMN_N_PAGES, book.numPages());
        cv.put(COLUMN_DESCRIPTION, book.description());
        cv.put(COLUMN_CATEGORY_ID, book.category().id().getValue());

        String where = String.format("%s = %d", COLUMN_ID, book.id().getValue());
        if (executor.execUpdate(TABLE_NAME, where, cv) == 0) {
            cv.put(COLUMN_ID, book.id().getValue());
            executor.execInsert(TABLE_NAME, cv);
        }
    }

}
