package com.at.cancerbero.domain.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import java8.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;


public abstract class CacheMap<K, V> {
    private final Long timeout;
    private Map<K, Holder<V>> values = Collections.synchronizedMap(new HashMap<>());

    public CacheMap(Long timeout, TimeUnit units) {
        this.timeout = TimeUnit.MILLISECONDS.convert(timeout, units);
    }

    public CompletableFuture<V> get(K key) {
        CompletableFuture<V> result = new CompletableFuture<>();
        if (validKey(key)) {
            result.complete(values.get(key).value);
        } else {
            result = getFreshContent(key).thenApply(newData -> {
                values.put(key, new Holder<>(
                        System.currentTimeMillis() + timeout,
                        newData
                ));
                return newData;
            });
        }
        return result;
    }

    private boolean validKey(K key) {
        Holder<V> value = values.get(key);
        return (value != null) && (value.expired >= System.currentTimeMillis());
    }

    public void invalidate(K key) {
        values.remove(key);
    }

    protected abstract CompletableFuture<V> getFreshContent(K key);

    @AllArgsConstructor
    private class Holder<V> {
        private final Long expired;
        private final V value;
    }
}
