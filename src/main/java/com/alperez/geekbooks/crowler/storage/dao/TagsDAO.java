package com.alperez.geekbooks.crowler.storage.dao;

import com.alperez.geekbooks.crowler.data.LongId;
import com.alperez.geekbooks.crowler.data.TagModel;
import com.alperez.geekbooks.crowler.storage.DbTableManager;
import com.alperez.geekbooks.crowler.storage.executor.DbExecutor;

import java.sql.Connection;
import java.sql.SQLException;

public class TagsDAO implements DbTableManager {
    private static final String TABLE_NAME = "Tags";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TITLE = "title";


    private final DbExecutor executor;

    public TagsDAO(Connection connection) {
        executor = new DbExecutor(connection);
    }

    @Override
    public void createTable() throws SQLException {
        String sql = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT);",
                TABLE_NAME,
                COLUMN_ID,
                COLUMN_TITLE);
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

    public TagModel insertTag(String tagTitle) throws SQLException {
        String sql = String.format("INSERT INTO %s (%s) VALUES ('%s');", TABLE_NAME, COLUMN_TITLE, tagTitle);
        long id = executor.execUpdateOutId(sql);
        return TagModel.create(LongId.valueOf(id), tagTitle);
    }
}
