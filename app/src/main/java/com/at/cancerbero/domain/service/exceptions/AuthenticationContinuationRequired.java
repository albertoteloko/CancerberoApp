package com.at.cancerbero.domain.service.exceptions;

import com.at.cancerbero.domain.service.handlers.AuthenticationContinuations;

public class AuthenticationContinuationRequired extends RuntimeException {
    public final AuthenticationContinuations authenticationContinuations;

    public AuthenticationContinuationRequired(AuthenticationContinuations authenticationContinuations) {
        super("Continuation required: " + authenticationContinuations);
        this.authenticationContinuations = authenticationContinuations;
    }
}
