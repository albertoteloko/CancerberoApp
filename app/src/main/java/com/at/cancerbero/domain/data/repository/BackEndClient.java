package com.at.cancerbero.domain.data.repository;

import com.at.cancerbero.domain.data.repository.server.ServerConnector;

public class BackEndClient extends ServerConnector {

    public BackEndClient(String baseUrl, boolean ignoreHostVerification) {
        super(baseUrl, ignoreHostVerification);
    }

    public void setToken(String token) {
        getCommonHeaders().put("Authorization", token);
    }
}