package com.alperez.geekbooks.crowler.storage.dao;

import com.alperez.geekbooks.crowler.storage.executor.DbExecutor;

import java.sql.Connection;
import java.sql.SQLException;

public class BooksDAO {
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
        executor.execUpdate(sql);
    }

    public void dropTable() throws SQLException {
        executor.execUpdate(String.format("drop table %s;", TABLE_NAME));
    }

}
