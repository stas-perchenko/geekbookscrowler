package com.alperez.geekbooks.crowler.storage;

import com.alperez.geekbooks.crowler.data.*;
import com.alperez.geekbooks.crowler.data.dbmodel.AuthorModel;
import com.alperez.geekbooks.crowler.data.dbmodel.BookCategoryModel;
import com.alperez.geekbooks.crowler.data.dbmodel.BookModel;
import com.alperez.geekbooks.crowler.storage.dao.*;
import com.alperez.geekbooks.crowler.utils.Log;
import com.alperez.geekbooks.crowler.utils.NonNull;
import com.alperez.geekbooks.crowler.utils.TextUtils;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class BookDbSaver implements Closeable {
    public static final String LOG_TAG = "DB_SAVE";

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

    private void initTable(DbTableManager tableManager) throws SQLException {
        if (tableManager.isTableExist()) {
            tableManager.dropTable();
        }
        tableManager.createTable();
    }

    public void initAllTables() throws SQLException {
        if (isClosed()) throw new IllegalStateException("Already closed");

        synchronized (mConnection) {
            try {
                mConnection.setAutoCommit(false);
                initTable(new BooksDAO(mConnection));
                initTable(new AuthorsDAO(mConnection));
                initTable(new TagsDAO(mConnection));
                initTable(new CategoriesDAO(mConnection));
                initTable(new BookAuthorsReferenceDAO(mConnection));
                initTable(new BookTagsReferenceDAO(mConnection));
                initTable(new BookRelationsDAO(mConnection));
                mConnection.commit();
            } catch (SQLException e) {
                mConnection.rollback();
                throw e;
            } finally {
                mConnection.setAutoCommit(true);
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
                new BookRelationsDAO(mConnection).dropTable();
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
                new BookRelationsDAO(mConnection).createTable();
                mConnection.commit();
            } catch (SQLException e) {
                mConnection.rollback();
                throw e;
            } finally {
                mConnection.setAutoCommit(true);
            }
        }
    }


    private final IdModelCache<AuthorModel> authorsCache = new IdModelCache<>();
    private final IdModelCache<TagModel> tagssCache = new IdModelCache<>();
    private final IdModelCache<BookCategoryModel> categoryCache = new IdModelCache<>();

    public void clearCachedData() {
        synchronized (mConnection) {
            authorsCache.clear();
            tagssCache.clear();
            categoryCache.clear();
        }
    }


    public void insertBook(BookModel book) throws SQLException {
        if (isClosed()) throw new IllegalStateException("Already closed");
        AuthorsDAO authDao = new AuthorsDAO(mConnection);
        BookAuthorsReferenceDAO bookAuthDao = new BookAuthorsReferenceDAO(mConnection);
        TagsDAO tagDao = new TagsDAO(mConnection);
        BookTagsReferenceDAO bookTagDao = new BookTagsReferenceDAO(mConnection);
        CategoriesDAO categDao = new CategoriesDAO(mConnection);
        BookRelationsDAO bookRelDao = new BookRelationsDAO(mConnection);
        BooksDAO bookDao = new BooksDAO(mConnection);
        synchronized (mConnection) {
            int order;

            //----  Save Authors and book-author relations for a book  ----
            order = 0;
            for (AuthorModel auth : book.authors()) {
                if (!authorsCache.contains(auth)) {
                    try {
                        authorsCache.put(authDao.insertAuthor(auth));
                        Log.d(LOG_TAG, "Author has been inserted - %s;", auth);
                    } catch (SQLException e) {
                        Log.d(LOG_TAG, "<~~~ Error insert Author %s - %s", auth, e.getMessage());
                        e.printStackTrace(System.out);
                    }
                }
                try {
                    bookAuthDao.insertRelation(book.id(), auth.id(), order++);
                } catch (SQLException e) {
                    Log.d(LOG_TAG, "<~~~ Error insert Book-Author relation (%s : %s) - %s", book.title(), auth, e.getMessage());
                    e.printStackTrace(System.out);
                }
            }

            //----  Save Tags and book-tags relations for a book  ----
            order = 0;
            for (TagModel tag : book.tags()) {
                LongId<TagModel> tagId = tag.id();
                if ((tagId == null) || (tagId.getValue() <= 0)) {
                    TagModel existingTag = tagssCache.getFirstEqualsItem(item -> item.title().equals(tag.title()));
                    if (existingTag == null) {
                        try {
                            existingTag = tagDao.insertTag(tag.title());
                            Log.d(LOG_TAG, "<--- Tag '%s' has been saved - %s;", tag.title(), existingTag.toString());
                            tagssCache.put(existingTag);
                        } catch (SQLException e) {
                            Log.d(LOG_TAG, "<~~~ Error insert Tag %s - %s", tag.title(), e.getMessage());
                            e.printStackTrace(System.out);
                        }
                    }
                    tagId = existingTag.id();
                }
                try {
                    bookTagDao.insertRelation(book.id(), tagId, order++);
                } catch (SQLException e) {
                    Log.d(LOG_TAG, "<~~~ Error insert Book-Tag relation (%s : %s) - %s", book.title(), tag.title(), e.getMessage());
                    e.printStackTrace(System.out);
                }
            }

            //----  Check if Book Category exists, save if it doesn't  ----
            BookCategoryModel categ = book.category();
            do {
                if (!categoryCache.contains(categ)) {
                    try {
                        categDao.insertCategory(categ);
                        Log.d(LOG_TAG, "<--- New book Category has been inserted - %s;", categ);
                    } catch (SQLException e) {
                        Log.d(LOG_TAG, "<~~~ Error insert category %s - %s", categ, e.getMessage());
                        e.printStackTrace(System.out);
                    }
                    categoryCache.put(categ);
                }
                categ = categ.parent();
            } while (categ != null);

            //----  Save book-to-book relations for a book  ----
            bookRelDao.removeReferencesForBooks(book.id());
            order = 0;
            for (LongId<BookModel> relId : book.relatedBookIds()) {
                try {
                    bookRelDao.insertBookRelation(book, relId, order++);
                } catch (SQLException e) {
                    Log.d(LOG_TAG, "<~~~ Error insert Book-to-Book relation (%s : %s) - %s", book.title(), relId, e.getMessage());
                    throw e;
                }
            }

            //----  Save Book entity  ----
            try {
                bookDao.createOrUpdateBook(book);
                Log.d(LOG_TAG, "<--- New book has been inserted - \"%s\";", book.title());
            } catch (SQLException e) {
                Log.d(LOG_TAG, "<~~~ Error insert Book entity %s - %s", book.title(), e.getMessage());
                throw e;
            }
        }
    }

}
