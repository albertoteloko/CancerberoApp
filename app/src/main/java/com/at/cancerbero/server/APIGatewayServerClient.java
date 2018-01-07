package com.at.cancerbero.server;

import com.at.cancerbero.model.Installation;
import com.at.cancerbero.model.Installations;
import com.at.cancerbero.utils.server.ServerConnector;

import java.util.Set;

public class APIGatewayServerClient extends ServerConnector {

    public APIGatewayServerClient(String baseUrl, boolean ignoreHostVerification) {
        super(baseUrl, ignoreHostVerification);
    }


    public void setToken(String token) {
        getCommonHeaders().put("Authorization", token);
    }

    public Set<Installation> loadInstallations() {
        return get("/installations", Installations.class).getInstallations();
    }
}