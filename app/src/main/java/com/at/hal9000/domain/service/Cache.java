package com.at.hal9000.domain.service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import java8.util.concurrent.CompletableFuture;


public abstract class Cache<V> {
    private final Long timeout;
    private V data;
    private AtomicLong lastRetrieved = new AtomicLong(-1);
    private ReentrantLock lock = new ReentrantLock();

    public Cache(Long timeout, TimeUnit units) {
        this.timeout = TimeUnit.MILLISECONDS.convert(timeout, units);
    }

    public CompletableFuture<V> getContent() {
        CompletableFuture<V> result = new CompletableFuture<>();
        lock.lock();
        try {
            if (mustReloadCache()) {
                result = getFreshContent().thenApply(newData -> {
                    data = newData;
                    lastRetrieved.set(System.currentTimeMillis());
                    return newData;
                });
            } else {
                result.complete(data);
            }
        } finally {
            lock.unlock();
        }
        return result;
    }

    private boolean mustReloadCache() {
        long maxTime = lastRetrieved.get() + timeout;
        return (lastRetrieved.get() == -1) || (maxTime < System.currentTimeMillis());
    }

    public void invalidate() {
        lastRetrieved.set(-1);
    }

    protected abstract CompletableFuture<V> getFreshContent();

}
