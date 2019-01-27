package com.alperez.geekbooks.crowler.data;

public class LongId<T extends IdProvidingModel> {
    private final long value;
    private final int hash;

    public static <E extends IdProvidingModel> LongId<E> valueOf(long value) {
        return new LongId<>(value);
    }

    private LongId(long value) {
        this.value = value;
        hash = Long.valueOf(value).hashCode();
    }

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LongId) {
            return ((LongId) o).value == this.value;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
