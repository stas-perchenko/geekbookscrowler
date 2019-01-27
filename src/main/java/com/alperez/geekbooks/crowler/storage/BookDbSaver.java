package com.alperez.geekbooks.crowler.storage;

import com.alperez.geekbooks.crowler.data.*;
import com.alperez.geekbooks.crowler.data.dbmodel.AuthorModel;
import com.alperez.geekbooks.crowler.data.dbmodel.BookCategoryModel;
import com.alperez.geekbooks.crowler.data.dbmodel.BookModel;
import com.alperez.geekbooks.crowler.storage.dao.*;
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

            //----  Save Authors and book-author relations for a book  ----
            for (AuthorModel auth : book.authors()) {
                if (!authorsCache.contains(auth)) authorsCache.put(authDao.insertAuthor(auth));
                try {
                    bookAuthDao.insertRelation(book.id(), auth.id());
                } catch (SQLException e) {}
            }

            //----  Save Tags and book-tags relations for a book  ----
            for (TagModel tag : book.tags()) {
                LongId<TagModel> tagId = tag.id();
                if ((tagId == null) || (tagId.getValue() <= 0)) {
                    TagModel existingTag = tagssCache.getFirstEqualsItem(item -> item.title().equals(tag.title()));
                    if (existingTag == null) {
                        existingTag = tagDao.insertTag(tag.title());
                        tagssCache.put(existingTag);
                    }
                    tagId = existingTag.id();
                }
                try {
                    bookTagDao.insertRelation(book.id(), tagId);
                } catch (SQLException e) { }
            }

            //----  Save category book-category relation to a book  ----
            BookCategoryModel categ = book.category();
            do {
                if (!categoryCache.contains(categ)) {
                    try {
                        categDao.insertCategory(categ);
                    } catch (SQLException e) { }
                    categoryCache.put(categ);
                }
                categ = categ.parent();
            } while (categ != null);

            //----  Save book-to-book relations for a book  ----
            bookRelDao.removeReferencesForBooks(book.id());
            int order = 0;
            for (LongId<BookModel> relId : book.relatedBookIds()) {
                bookRelDao.insertBookRelation(book, relId, order++);
            }

            //----  Save Book entity  ----
            bookDao.createOrUpdateBook(book);
        }
    }

}
