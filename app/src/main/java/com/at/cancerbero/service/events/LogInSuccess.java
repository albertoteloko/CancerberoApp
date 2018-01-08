package com.at.cancerbero.service.events;


import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class LogInSuccess implements Event {
    public final CognitoUserSession cognitoUserSession;
    public final CognitoDevice device;

    public LogInSuccess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
        this.cognitoUserSession = cognitoUserSession;
        this.device = device;
    }
}
