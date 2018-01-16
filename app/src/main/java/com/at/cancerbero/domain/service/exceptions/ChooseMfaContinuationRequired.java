package com.at.cancerbero.domain.service.exceptions;

public class ChooseMfaContinuationRequired extends RuntimeException {
    public ChooseMfaContinuationRequired() {
    }

    public ChooseMfaContinuationRequired(String message) {
        super(message);
    }

    public ChooseMfaContinuationRequired(String message, Throwable cause) {
        super(message, cause);
    }

    public ChooseMfaContinuationRequired(Throwable cause) {
        super(cause);
    }

    public ChooseMfaContinuationRequired(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
