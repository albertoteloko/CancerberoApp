package com.at.cancerbero.domain.data.repository.server;

public class UnexpectedCodeException extends RuntimeException {
    public final int code;
    public final String body;

    public UnexpectedCodeException(int code, String body) {
        super("Unexpected code " + code);
        this.code = code;
        this.body = body;
    }


    public UnexpectedCodeException(int code, String body, Exception e) {
        super("Unexpected code " + code, e);
        this.code = code;
        this.body = body;
    }
}
