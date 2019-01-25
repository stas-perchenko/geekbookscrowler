package com.alperez.geekbooks.crowler.storage.dao;

import com.alperez.geekbooks.crowler.data.TagModel;
import com.alperez.geekbooks.crowler.storage.executor.DbExecutor;

import java.sql.Connection;
import java.sql.SQLException;

public class TagsDAO {
    private static final String TABLE_NAME = "Tags";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TITLE = "title";


    private final DbExecutor executor;

    public TagsDAO(Connection connection) {
        executor = new DbExecutor(connection);
    }

    public void createTable() throws SQLException {
        String sql = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT);",
                TABLE_NAME,
                COLUMN_ID,
                COLUMN_TITLE);
        executor.execUpdate(sql);
    }

    public void dropTable() throws SQLException {
        executor.execUpdate(String.format("drop table %s;", TABLE_NAME));
    }

    public TagModel insertTag(String tagTitle) {

    }
}
