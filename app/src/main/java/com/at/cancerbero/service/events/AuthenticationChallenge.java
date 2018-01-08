package com.at.cancerbero.service.events;


import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class AuthenticationChallenge implements Event {
    public final ChallengeContinuation continuation;

    public AuthenticationChallenge(ChallengeContinuation continuation) {
        this.continuation = continuation;
    }
}
