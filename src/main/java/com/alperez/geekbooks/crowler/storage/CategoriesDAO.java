package com.alperez.geekbooks.crowler.storage;

import com.alperez.geekbooks.crowler.storage.executor.DbExecutor;

import java.sql.Connection;
import java.sql.SQLException;

public class CategoriesDAO {
    private static final String TABLE_NAME = "Categories";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_LEVEL = "level";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_PARENT_ID = "parent_id";




    private final DbExecutor executor;

    public CategoriesDAO(Connection connection) {
        executor = new DbExecutor(connection);
    }

    public void createTable() throws SQLException {
        String sql = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER, %s TEXT, %s INTEGER);",
                TABLE_NAME,
                COLUMN_ID,
                COLUMN_LEVEL,
                COLUMN_TITLE,
                COLUMN_PARENT_ID);
        executor.execUpdate(sql);
    }

    public void dropTable() throws SQLException {
        executor.execUpdate(String.format("drop table %s;", TABLE_NAME));
    }
}
