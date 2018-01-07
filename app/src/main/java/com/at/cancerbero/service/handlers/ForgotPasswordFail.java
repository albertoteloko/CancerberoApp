package com.at.cancerbero.service.handlers;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class ForgotPasswordFail implements Event {
    public final String userId;
    public final Exception exception;

    public ForgotPasswordFail(String userId, Exception exception) {
        this.userId = userId;
        this.exception = exception;
    }
}
