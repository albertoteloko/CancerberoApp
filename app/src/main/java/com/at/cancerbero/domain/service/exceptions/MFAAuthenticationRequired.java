package com.at.cancerbero.domain.service.exceptions;

public class MFAAuthenticationRequired extends RuntimeException {
    public MFAAuthenticationRequired() {
    }

    public MFAAuthenticationRequired(String message) {
        super(message);
    }

    public MFAAuthenticationRequired(String message, Throwable cause) {
        super(message, cause);
    }

    public MFAAuthenticationRequired(Throwable cause) {
        super(cause);
    }

    public MFAAuthenticationRequired(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
