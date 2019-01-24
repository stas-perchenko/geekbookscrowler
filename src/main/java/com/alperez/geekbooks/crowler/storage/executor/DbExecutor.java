package com.alperez.geekbooks.crowler.storage.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DbExecutor {

    private final Connection connection;

    public DbExecutor(Connection connection) {
        this.connection = connection;
    }


    public void execUpdate(String update) throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute(update);
        stmt.close();
    }

}
