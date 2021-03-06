package com.alperez.geekbooks.crowler.storage.dao;

import com.alperez.geekbooks.crowler.data.dbmodel.AuthorModel;
import com.alperez.geekbooks.crowler.storage.DbTableManager;
import com.alperez.geekbooks.crowler.storage.executor.ContentValue;
import com.alperez.geekbooks.crowler.storage.executor.DbExecutor;

import java.sql.Connection;
import java.sql.SQLException;

public class AuthorsDAO implements DbTableManager {
    private static final String TABLE_NAME = "Authors";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_FAMILY_NAME = "family_name";




    private final DbExecutor executor;

    public AuthorsDAO(Connection connection) {
        executor = new DbExecutor(connection);
    }

    @Override
    public void createTable() throws SQLException {
        String sql = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT);",
                TABLE_NAME,
                COLUMN_ID,
                COLUMN_NAME,
                COLUMN_FAMILY_NAME);
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

    public AuthorModel insertAuthor(AuthorModel author) throws SQLException {
        ContentValue cv = new ContentValue();
        cv.put(COLUMN_ID, author.id().getValue());
        cv.put(COLUMN_FAMILY_NAME, author.familyName());
        if (author.name() != null) cv.put(COLUMN_NAME, author.name());
        executor.execInsert(TABLE_NAME, cv);
        return author;
    }
}
