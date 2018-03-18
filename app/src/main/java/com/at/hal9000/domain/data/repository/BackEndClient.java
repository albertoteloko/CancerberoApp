package com.at.hal9000.domain.data.repository;

import com.at.hal9000.domain.data.repository.server.ServerConnector;

public class BackEndClient extends ServerConnector {

    public BackEndClient(String baseUrl, boolean ignoreHostVerification) {
        super(baseUrl, ignoreHostVerification);
    }

    public void setToken(String token) {
        getCommonHeaders().put("Authorization", token);
    }
}