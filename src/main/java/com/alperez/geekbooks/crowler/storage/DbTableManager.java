package com.alperez.geekbooks.crowler.storage;

import java.sql.SQLException;

/**
 * This interface is to be implemented by DAO classes.
 * It defines ability to manage tables in DB
 */
public interface DbTableManager {
    void dropTable() throws SQLException;
    void createTable() throws SQLException;
    boolean isTableExist() throws SQLException;
}
