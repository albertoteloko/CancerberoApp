package com.at.cancerbero.installations.repository;

import com.at.cancerbero.utils.server.ServerConnector;

public class BackEndClient extends ServerConnector {

    public BackEndClient(String baseUrl, boolean ignoreHostVerification) {
        super(baseUrl, ignoreHostVerification);
    }

    public void setToken(String token) {
        getCommonHeaders().put("Authorization", token);
    }
}