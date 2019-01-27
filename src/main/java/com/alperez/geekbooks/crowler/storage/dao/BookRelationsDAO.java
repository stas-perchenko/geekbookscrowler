package com.alperez.geekbooks.crowler.storage.dao;

import com.alperez.geekbooks.crowler.data.LongId;
import com.alperez.geekbooks.crowler.data.dbmodel.BookModel;
import com.alperez.geekbooks.crowler.storage.executor.DbExecutor;

import java.sql.Connection;
import java.sql.SQLException;

public class BookRelationsDAO {
    private static final String TABLE_NAME = "Authors";
    private static final String COLUMN_HOST_BOOK_ID   = "host_book_id";
    private static final String COLUMN_DEPEND_BOOK_ID = "depend_book_id";
    private static final String COLUMN_ORDER = "order";

    private final DbExecutor executor;

    public BookRelationsDAO(Connection connection) {
        executor = new DbExecutor(connection);
    }

    public void createTable() throws SQLException {
        String sql = String.format("CREATE TABLE %1$s (%2$s INTEGER, %3$s INTEGER, %4$s INTEGER, PRIMARY KEY(%2$s, %3$s));",
                TABLE_NAME,
                COLUMN_HOST_BOOK_ID,
                COLUMN_DEPEND_BOOK_ID,
                COLUMN_ORDER);
        executor.execUpdate(sql);
    }

    public void dropTable() throws SQLException {
        executor.execUpdate(String.format("drop table %s;", TABLE_NAME));
    }

    public int removeReferencesForBooks(LongId<BookModel> bookId) throws SQLException {
        String sql = String.format("DELETE FROM %s WHERE (%s = %d);", TABLE_NAME, COLUMN_HOST_BOOK_ID, bookId.getValue());
        return executor.execUpdate(sql);
    }

    public void insertBookRelation(BookModel hostBook, LongId<BookModel> dependBookId, int order) throws SQLException {
        if (dependBookId.equals(hostBook.id())) {
            throw new IllegalArgumentException("A book cannot depend on itself - "+hostBook.title());
        } else if (order < 0) {
            throw new IllegalArgumentException("Order must be non-negative");
        } else {
            String sql = String.format("INSERT INTO %1$s (%2$s, %4$s, %6$s) VALUES (%3$s, %5$s, %7$s);",
                    TABLE_NAME,
                    COLUMN_HOST_BOOK_ID, hostBook.id().getValue(),
                    COLUMN_DEPEND_BOOK_ID, dependBookId.getValue(),
                    COLUMN_ORDER, order);
            executor.execUpdate(sql);
        }
    }
}
