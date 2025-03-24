package com.tcoded.hologramlib.types;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class ArrayQueue <T> extends ArrayList<T> implements Queue<T> {

    @Override
    public boolean offer(T t) {
        return false;
    }

    @Override
    public T remove() {
        if (isEmpty()) throw new NoSuchElementException("Queue is empty");
        return removeFirst();
    }

    @Override
    public T poll() {
        return null;
    }

    @Override
    public T element() {
        return null;
    }

    @Override
    public T peek() {
        return null;
    }

}
