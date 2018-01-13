package com.at.cancerbero.service.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class ServerError implements Event {
    public final Exception exception;

    public ServerError(Exception exception) {
        this.exception = exception;
    }
}