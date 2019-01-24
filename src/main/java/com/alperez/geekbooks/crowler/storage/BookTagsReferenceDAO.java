package com.alperez.geekbooks.crowler.storage;

import com.alperez.geekbooks.crowler.storage.executor.DbExecutor;

import java.sql.Connection;
import java.sql.SQLException;

public class BookTagsReferenceDAO {
    private static final String TABLE_NAME = "BooksTags";
    private static final String COLUMN_BOOK_ID = "book_id";
    private static final String COLUMN_TAG_ID = "tag_id";


    private final DbExecutor executor;

    public BookTagsReferenceDAO(Connection connection) {
        executor = new DbExecutor(connection);
    }

    public void createTable() throws SQLException {
        String sql = String.format("CREATE TABLE %1$s (%2$s INTEGER, %3$s INTEGER, PRIMARY KEY(%2$s, %3$s));",
                TABLE_NAME,
                COLUMN_BOOK_ID,
                COLUMN_TAG_ID);
        executor.execUpdate(sql);
    }

    public void dropTable() throws SQLException {
        executor.execUpdate(String.format("drop table %s;", TABLE_NAME));
    }
}
