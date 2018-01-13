package com.at.cancerbero.service.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class UserDetailsFail implements Event {
    public final Exception exception;

    public UserDetailsFail(Exception exception) {
        this.exception = exception;
    }
}