package com.alperez.geekbooks.crowler.utils;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

public class StringBuilderWriter extends Writer implements Serializable {


    private static final long serialVersionUID = -146927496096066153L;
    private final StringBuilder builder;

    /**
     * Constructs a new {@link StringBuilder} instance with default capacity.
     */
    public StringBuilderWriter() {
        this.builder = new StringBuilder();
    }

    /**
     * Constructs a new {@link StringBuilder} instance with the specified capacity.
     *
     * @param capacity The initial capacity of the underlying {@link StringBuilder}
     */
    public StringBuilderWriter(final int capacity) {
        this.builder = new StringBuilder(capacity);
    }

    /**
     * Appends a single character to this Writer.
     *
     * @param value The character to append
     * @return This writer instance
     */
    @Override
    public Writer append(final char value) {
        builder.append(value);
        return this;
    }

    /**
     * Appends a character sequence to this Writer.
     *
     * @param value The character to append
     * @return This writer instance
     */
    @Override
    public Writer append(final CharSequence value) {
        builder.append(value);
        return this;
    }

    /**
     * Appends a portion of a character sequence to the {@link StringBuilder}.
     *
     * @param value The character to append
     * @param start The index of the first character
     * @param end The index of the last character + 1
     * @return This writer instance
     */
    @Override
    public Writer append(final CharSequence value, final int start, final int end) {
        builder.append(value, start, end);
        return this;
    }

    /**
     * Closing this writer has no effect.
     */
    @Override
    public void close() {
        // no-op
    }

    /**
     * Flushing this writer has no effect.
     */
    @Override
    public void flush() {
        // no-op
    }


    /**
     * Writes a String to the {@link StringBuilder}.
     *
     * @param value The value to write
     */
    @Override
    public void write(final String value) {
        if (value != null) {
            builder.append(value);
        }
    }

    /**
     * Writes a portion of a character array to the {@link StringBuilder}.
     *
     * @param value The value to write
     * @param offset The index of the first character
     * @param length The number of characters to write
     */
    @Override
    public void write(final char[] value, final int offset, final int length) {
        if (value != null) {
            builder.append(value, offset, length);
        }
    }

    /**
     * Returns {@link StringBuilder#toString()}.
     *
     * @return The contents of the String builder.
     */
    @Override
    public String toString() {
        return builder.toString();
    }
}
