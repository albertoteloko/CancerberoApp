package com.at.cancerbero.service.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class ServerError implements Event {
    public final Exception exceptions;

    public ServerError(Exception exceptions) {
        this.exceptions = exceptions;
    }
}
