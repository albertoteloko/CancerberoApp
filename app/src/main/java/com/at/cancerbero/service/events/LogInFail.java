package com.at.cancerbero.service.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class LogInFail implements Event {
    public final Exception exception;

    public LogInFail(Exception exception) {
        this.exception = exception;
    }
}
