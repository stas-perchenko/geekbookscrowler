package com.alperez.geekbooks.crowler.storage.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DbExecutor {

    private final Connection connection;

    public DbExecutor(Connection connection) {
        this.connection = connection;
    }


    public int execUpdate(String update) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(update);
        }
    }

    public boolean execInsert(String tablename, ContentValue data) throws SQLException {
        StringBuilder sbColumns = new StringBuilder('(');
        StringBuilder sbValues = new StringBuilder('(');
        boolean isFirst = true;
        for (String column : data.getKeys()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sbColumns.append(", ");
                sbValues.append(", ");
            }
            sbColumns.append(column);
            Object v = data.get(column);
            if (v instanceof String) {
                sbValues.append(String.format("'%s'", (String) v));
            } else if (v instanceof Number) {
                sbValues.append(((Number) v).toString());
            } else {
                sbValues.append(String.format("'%s'", v.toString()));
            }
        }
        sbColumns.append(')');
        sbValues.append(')');

        try (Statement stmt = connection.createStatement()) {
            String sql = String.format("INSERT INTO %s %s VALUES %s;", tablename, sbColumns.toString(), sbValues.toString());
            return (stmt.executeUpdate(sql) > 0);
        }
    }

    public int execUpdate(String tableName, String where, ContentValue data) throws SQLException {
        StringBuilder sbSet = new StringBuilder();
        boolean isFirst = true;
        for (String column : data.getKeys()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sbSet.append(", ");
            }

            Object v = data.get(column);
            if (v instanceof String) {
                sbSet.append(String.format("%s = '%s'", column, (String) v));
            } else if (v instanceof Number) {
                sbSet.append(String.format("%s = %s", column, ((Number) v).toString()));
            } else {
                sbSet.append(String.format("%s = '%s'", column, v.toString()));
            }
        }

        try (Statement stmt = connection.createStatement()) {
            String sql = String.format("UPDATE %s SET %s WHERE %s", tableName, sbSet.toString(), where);
            return stmt.executeUpdate(sql);
        }
    }

}
