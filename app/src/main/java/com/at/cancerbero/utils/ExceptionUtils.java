package com.at.cancerbero.utils;

public class ExceptionUtils {
    public static void throwRuntimeException(Exception e) throws RuntimeException {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new RuntimeException(e);
        }
    }
}
