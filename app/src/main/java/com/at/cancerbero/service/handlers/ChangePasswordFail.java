package com.at.cancerbero.service.handlers;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class ChangePasswordFail implements Event {
    public final Exception exception;

    public ChangePasswordFail(Exception exception) {
        this.exception = exception;
    }
}
