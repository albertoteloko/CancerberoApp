package com.at.cancerbero.utils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Cache<T> {
    private T data;
    private AtomicLong lastRetrieved = new AtomicLong(-1);
    private ReentrantLock lock = new ReentrantLock();

    public T getContent() {
        lock.lock();
        try {
            if (mustReloadCache()) {
                data = getFreshContent();
                lastRetrieved.set(System.currentTimeMillis());
            }
            return data;
        } finally {
            lock.unlock();
        }
    }

    private boolean mustReloadCache() {
        long maxTime = lastRetrieved.get() + getTimeout();
        return (lastRetrieved.get() == -1) || (maxTime < System.currentTimeMillis());
    }

    public void invalidate() {
        lastRetrieved.set(-1);
    }

    protected abstract T getFreshContent();

    protected abstract long getTimeout();
}
