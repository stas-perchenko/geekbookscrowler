package com.alperez.geekbooks.crowler.storage;

import com.alperez.geekbooks.crowler.data.IdProvidingModel;
import com.alperez.geekbooks.crowler.data.LongId;
import com.alperez.geekbooks.crowler.utils.NonNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class IdModelCache<T extends IdProvidingModel> {

    public interface ItemComparator<CM> {
        boolean onCompare(CM item);
    }

    private List<T> data = new LinkedList<>();


    public synchronized void clear() {
        data.clear();
    }

    public synchronized void put(T item) {
        for (Iterator<T> itr = data.iterator(); itr.hasNext(); ) {
            if (itr.next().id().getValue() == item.id().getValue()) return;
        }
        data.add(0, item);
    }

    public synchronized T get(LongId<T> id) {
        for (Iterator<T> itr = data.iterator(); itr.hasNext(); ) {
            T item = itr.next();
            if (id.equals(item.id())) return item;
        }
        return null;
    }

    public synchronized T remove(LongId<T> id) {
        for (Iterator<T> itr = data.iterator(); itr.hasNext(); ) {
            T item = itr.next();
            if (id.equals(item.id())) {
                itr.remove();
                return item;
            }
        }
        return null;
    }

    public synchronized boolean contains(T item) {
        for (Iterator<T> itr = data.iterator(); itr.hasNext(); ) {
            if (item.id().getValue() == itr.next().id().getValue()) return true;
        }
        return false;
    }

    public synchronized T getFirstEqualsItem(@NonNull ItemComparator<T> comparator) {
        for (Iterator<T> itr = data.iterator(); itr.hasNext(); ) {
            T item = itr.next();
            if (comparator.onCompare(item)) {
                return item;
            }
        }
        return null;
    }
}
