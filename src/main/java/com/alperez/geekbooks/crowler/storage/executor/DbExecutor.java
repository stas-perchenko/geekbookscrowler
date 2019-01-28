package com.alperez.geekbooks.crowler.storage.executor;

import com.alperez.geekbooks.crowler.utils.Log;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbExecutor {
    public static final boolean D = false;
    public static final String LOG_TAG = "DB_EXECUTOR";

    private final Connection connection;

    public DbExecutor(Connection connection) {
        this.connection = connection;
    }


    public int execUpdateNumAffected(String update) throws SQLException {
        if (D) Log.d(LOG_TAG, "execUpdateNumAffected(%s)", update);
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(update);
        }




    }

    public boolean isTableExist(String tableName) throws SQLException {
        if (D) Log.d(LOG_TAG, "isTableExist(%s)", tableName);
        try (Statement stmt = connection.createStatement()) {
            String q = String.format("SELECT * FROM sqlite_master WHERE type = 'table' AND name = '%s'", tableName);
            ResultSet rs = stmt.executeQuery(q);
            if (rs.getType() == ResultSet.TYPE_FORWARD_ONLY) {
                return rs.next();
            } else {
                return rs.first();
            }
        }
    }

    public long execUpdateOutId(String update) throws SQLException {
        if (D) Log.d(LOG_TAG, "execUpdateNumAffected(%s)", update);
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(update);
            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getLong(1);
        }
    }

    public boolean execInsert(String tablename, ContentValue data) throws SQLException {
        StringBuilder sbColumns = new StringBuilder("(");
        StringBuilder sbValues = new StringBuilder("(");
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
            if (D) Log.d(LOG_TAG, "execInsert() - %s", sql);
            try {
                return (stmt.executeUpdate(sql) > 0);
            } catch (SQLException e) {
                if (!D) Log.d(LOG_TAG, "execUpdateNumAffected() - %s", sql);
                throw e;
            }
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
            String sql = String.format("UPDATE %s SET %s WHERE %s;", tableName, sbSet.toString(), where);
            if (D) Log.d(LOG_TAG, "execUpdateNumAffected() - %s", sql);
            try {
                return stmt.executeUpdate(sql);
            } catch (SQLException e) {
                if (!D) Log.d(LOG_TAG, "execUpdateNumAffected() - %s", sql);
                throw e;
            }
        }
    }

}
