package com.at.cancerbero.service.events;


import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class ForgotPasswordSuccess implements Event {
    public final String userId;

    public ForgotPasswordSuccess(String userId) {
        this.userId = userId;
    }
}
