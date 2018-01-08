package com.at.cancerbero.service.events;


import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class MultiFactorAuthentication implements Event {
    public final MultiFactorAuthenticationContinuation continuation;

    public MultiFactorAuthentication(MultiFactorAuthenticationContinuation continuation) {
        this.continuation = continuation;
    }
}
