package com.at.cancerbero.domain.service.exceptions;

public class NewPasswordRequired extends RuntimeException {
    public NewPasswordRequired() {
    }

    public NewPasswordRequired(String message) {
        super(message);
    }

    public NewPasswordRequired(String message, Throwable cause) {
        super(message, cause);
    }

    public NewPasswordRequired(Throwable cause) {
        super(cause);
    }

    public NewPasswordRequired(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
