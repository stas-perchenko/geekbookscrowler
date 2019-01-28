package com.alperez.geekbooks.crowler.storage.dao;

import com.alperez.geekbooks.crowler.data.dbmodel.BookModel;
import com.alperez.geekbooks.crowler.data.LongId;
import com.alperez.geekbooks.crowler.data.TagModel;
import com.alperez.geekbooks.crowler.storage.DbTableManager;
import com.alperez.geekbooks.crowler.storage.executor.ContentValue;
import com.alperez.geekbooks.crowler.storage.executor.DbExecutor;

import java.sql.Connection;
import java.sql.SQLException;

public class BookTagsReferenceDAO implements DbTableManager {
    private static final String TABLE_NAME = "BooksTags";
    private static final String COLUMN_BOOK_ID = "book_id";
    private static final String COLUMN_TAG_ID = "tag_id";
    private static final String COLUMN_ORDER = "_order_";


    private final DbExecutor executor;

    public BookTagsReferenceDAO(Connection connection) {
        executor = new DbExecutor(connection);
    }

    @Override
    public void createTable() throws SQLException {
        String sql = String.format("CREATE TABLE %1$s (%2$s INTEGER, %3$s INTEGER, %4$s INTEGER, PRIMARY KEY(%2$s, %3$s));",
                TABLE_NAME,
                COLUMN_BOOK_ID,
                COLUMN_TAG_ID,
                COLUMN_ORDER);
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

    public void insertRelation(LongId<BookModel> bookId, LongId<TagModel> tagId, int order) throws SQLException {
        ContentValue cv = new ContentValue();
        cv.put(COLUMN_BOOK_ID, bookId.getValue());
        cv.put(COLUMN_TAG_ID, tagId.getValue());
        cv.put(COLUMN_ORDER, order);
        executor.execInsert(TABLE_NAME, cv);
    }
}
