package com.alperez.geekbooks.crowler.storage.dao;

import com.alperez.geekbooks.crowler.data.AuthorModel;
import com.alperez.geekbooks.crowler.data.BookModel;
import com.alperez.geekbooks.crowler.data.LongId;
import com.alperez.geekbooks.crowler.storage.executor.DbExecutor;

import java.sql.Connection;
import java.sql.SQLException;

public class BookAuthorsReferenceDAO {
    private static final String TABLE_NAME = "BooksAuthors";
    private static final String COLUMN_BOOK_ID = "book_id";
    private static final String COLUMN_AUTH_ID = "auth_id";


    private final DbExecutor executor;

    public BookAuthorsReferenceDAO(Connection connection) {
        executor = new DbExecutor(connection);
    }

    public void createTable() throws SQLException {
        String sql = String.format("CREATE TABLE %1$s (%2$s INTEGER, %3$s INTEGER, PRIMARY KEY(%2$s, %3$s));",
                TABLE_NAME,
                COLUMN_BOOK_ID,
                COLUMN_AUTH_ID);
        executor.execUpdate(sql);
    }

    public void dropTable() throws SQLException {
        executor.execUpdate(String.format("drop table %s;", TABLE_NAME));
    }

    public void insertRelation(LongId<BookModel> bookId, LongId<AuthorModel> authId) throws SQLException {
        //TODO Implement this !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        fgdfgdfg
    }
}
