package com.alperez.geekbooks.crowler.storage.dao;

import com.alperez.geekbooks.crowler.data.dbmodel.BookCategoryModel;
import com.alperez.geekbooks.crowler.storage.DbTableManager;
import com.alperez.geekbooks.crowler.storage.executor.ContentValue;
import com.alperez.geekbooks.crowler.storage.executor.DbExecutor;

import java.sql.Connection;
import java.sql.SQLException;

public class CategoriesDAO implements DbTableManager {
    private static final String TABLE_NAME = "Categories";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_LEVEL = "level";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_PARENT_ID = "parent_id";




    private final DbExecutor executor;

    public CategoriesDAO(Connection connection) {
        executor = new DbExecutor(connection);
    }

    @Override
    public void createTable() throws SQLException {
        String sql = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER, %s TEXT, %s INTEGER);",
                TABLE_NAME,
                COLUMN_ID,
                COLUMN_LEVEL,
                COLUMN_TITLE,
                COLUMN_PARENT_ID);
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

    public void insertCategory(BookCategoryModel category) throws SQLException {
        ContentValue cv = new ContentValue();
        cv.put(COLUMN_ID, category.id().getValue());
        cv.put(COLUMN_LEVEL, category.level());
        cv.put(COLUMN_TITLE, category.title());
        if (category.parent() != null) cv.put(COLUMN_PARENT_ID, category.parent().id().getValue());
        executor.execInsert(TABLE_NAME, cv);
    }
}
