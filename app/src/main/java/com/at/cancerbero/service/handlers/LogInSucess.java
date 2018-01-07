package com.at.cancerbero.service.handlers;


import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class LogInSucess implements Event {
    public final CognitoUserSession cognitoUserSession;
    public final CognitoDevice device;

    public LogInSucess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
        this.cognitoUserSession = cognitoUserSession;
        this.device = device;
    }
}
