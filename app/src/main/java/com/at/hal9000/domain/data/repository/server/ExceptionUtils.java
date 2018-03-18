package com.at.hal9000.domain.data.repository.server;

public class ExceptionUtils {
    public static void throwRuntimeException(Exception e) throws RuntimeException {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new RuntimeException(e);
        }
    }
}
