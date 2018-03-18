package com.at.hal9000.domain.service;

import java8.util.function.Supplier;

public class ReentrantLock extends java.util.concurrent.locks.ReentrantLock {

    public <T> T get(Supplier<T> supplier) {
        lock();
        try {
            return supplier.get();
        } finally {
            unlock();
        }
    }

    public void run(Runnable runnable) {
        get(() -> {
            runnable.run();
            return null;
        });
    }
}
