package com.at.cancerbero.service.events;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class ForgotPasswordStart implements Event {
    public final String userId;

    public ForgotPasswordStart(String userId) {
        this.userId = userId;
    }
}
