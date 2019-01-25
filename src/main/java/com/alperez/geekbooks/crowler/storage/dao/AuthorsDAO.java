package com.alperez.geekbooks.crowler.storage.dao;

import com.alperez.geekbooks.crowler.data.AuthorModel;
import com.alperez.geekbooks.crowler.data.LongId;
import com.alperez.geekbooks.crowler.storage.executor.DbExecutor;

import java.sql.Connection;
import java.sql.SQLException;

public class AuthorsDAO {
    private static final String TABLE_NAME = "Authors";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_FAMILY_NAME = "family_name";




    private final DbExecutor executor;

    public AuthorsDAO(Connection connection) {
        executor = new DbExecutor(connection);
    }

    public void createTable() throws SQLException {
        String sql = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT);",
                TABLE_NAME,
                COLUMN_ID,
                COLUMN_NAME,
                COLUMN_FAMILY_NAME);
        executor.execUpdate(sql);
    }

    public void dropTable() throws SQLException {
        executor.execUpdate(String.format("drop table %s;", TABLE_NAME));
    }

    public AuthorModel insertAuthor(AuthorModel author) {
        //TODO Implement this !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }
}
