package com.alperez.geekbooks.crowler.storage.dao;

import com.alperez.geekbooks.crowler.data.dbmodel.AuthorModel;
import com.alperez.geekbooks.crowler.data.dbmodel.BookModel;
import com.alperez.geekbooks.crowler.data.LongId;
import com.alperez.geekbooks.crowler.storage.executor.ContentValue;
import com.alperez.geekbooks.crowler.storage.executor.DbExecutor;

import java.sql.Connection;
import java.sql.SQLException;

public class BookAuthorsReferenceDAO {
    private static final String TABLE_NAME = "BooksAuthors";
    private static final String COLUMN_BOOK_ID = "book_id";
    private static final String COLUMN_AUTH_ID = "auth_id";
    private static final String COLUMN_ORDER = "order";


    private final DbExecutor executor;

    public BookAuthorsReferenceDAO(Connection connection) {
        executor = new DbExecutor(connection);
    }

    public void createTable() throws SQLException {
        String sql = String.format("CREATE TABLE %1$s (%2$s INTEGER, %3$s INTEGER, %4$s INTEGER, PRIMARY KEY(%2$s, %3$s));",
                TABLE_NAME,
                COLUMN_BOOK_ID,
                COLUMN_AUTH_ID,
                COLUMN_ORDER);
        executor.execUpdate(sql);
    }

    public void dropTable() throws SQLException {
        executor.execUpdate(String.format("drop table %s;", TABLE_NAME));
    }

    public void insertRelation(LongId<BookModel> bookId, LongId<AuthorModel> authId, int order) throws SQLException {
        ContentValue cv = new ContentValue();
        cv.put(COLUMN_BOOK_ID, bookId.getValue());
        cv.put(COLUMN_AUTH_ID, authId.getValue());
        cv.put(COLUMN_ORDER, order);
        executor.execInsert(TABLE_NAME, cv);
    }
}
