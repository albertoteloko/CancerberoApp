package com.at.cancerbero.service.events;


import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class UserDetailsSuccess implements Event {
    public CognitoUserDetails userDetails;

    public UserDetailsSuccess(CognitoUserDetails userDetails) {
        this.userDetails = userDetails;
    }
}
