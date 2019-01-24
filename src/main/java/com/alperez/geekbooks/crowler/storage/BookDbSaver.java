package com.alperez.geekbooks.crowler.storage;

import com.alperez.geekbooks.crowler.data.BookModel;
import com.alperez.geekbooks.crowler.utils.NonNull;
import com.alperez.geekbooks.crowler.utils.TextUtils;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class BookDbSaver implements Closeable {

    private final Connection mConnection;
    private boolean isClosed;

    public BookDbSaver(@NonNull String dbName) throws SQLException {
        if (TextUtils.isEmpty(dbName)) throw new IllegalArgumentException("No database name");
        mConnection = DriverManager.getConnection("jdbc:sqlite:"+dbName);
    }

    public boolean isClosed() {
        synchronized (mConnection) {
            return isClosed;
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (mConnection) {
            try {
                mConnection.close();
                isClosed = true;
            } catch (SQLException e) {
                throw new IOException("Error close DB connection", e);
            }
        }
    }

    public void dropAllTables() throws SQLException {
        if (isClosed()) throw new IllegalStateException("Already closed");

        synchronized (mConnection) {
            try {
                mConnection.setAutoCommit(false);
                new BooksDAO(mConnection).dropTable();
                new AuthorsDAO(mConnection).dropTable();
                new TagsDAO(mConnection).dropTable();
                new CategoriesDAO(mConnection).dropTable();
                new BookAuthorsReferenceDAO(mConnection).dropTable();
                new BookTagsReferenceDAO(mConnection).dropTable();
                mConnection.commit();
            } catch (SQLException e) {
                mConnection.rollback();
                throw e;
            } finally {
                mConnection.setAutoCommit(true);
            }
        }
    }

    public void createTables() throws SQLException {
        if (isClosed()) throw new IllegalStateException("Already closed");

        synchronized (mConnection) {
            try {
                mConnection.setAutoCommit(false);
                new BooksDAO(mConnection).createTable();
                new AuthorsDAO(mConnection).createTable();
                new TagsDAO(mConnection).createTable();
                new CategoriesDAO(mConnection).createTable();
                new BookAuthorsReferenceDAO(mConnection).createTable();
                new BookTagsReferenceDAO(mConnection).createTable();
                mConnection.commit();
            } catch (SQLException e) {
                mConnection.rollback();
                throw e;
            } finally {
                mConnection.setAutoCommit(true);
            }
        }
    }

    public void saveBook(BookModel book) {
        if (isClosed()) throw new IllegalStateException("Already closed");


        //TODO Implement this !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


    }
}
