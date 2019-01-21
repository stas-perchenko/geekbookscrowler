package com.alperez.geekbooks.crowler.storage;

import com.alperez.geekbooks.crowler.utils.NonNull;
import com.alperez.geekbooks.crowler.utils.TextUtils;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
                throw new RuntimeException(e);
            }
        }
    }
}
