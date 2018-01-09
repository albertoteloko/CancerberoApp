package com.at.cancerbero.installations.repository;

import com.at.cancerbero.installations.model.server.Installation;
import com.at.cancerbero.installations.model.server.Installations;
import com.at.cancerbero.utils.server.ServerConnector;

import java.util.Set;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InstallationRepository {

    private final BackEndClient serverConnector;

    public Set<Installation> loadInstallations() {
        return serverConnector.get("/installations", Installations.class, 200).getInstallations();
    }
}