package com.at.cancerbero.installations.repository;

import com.at.cancerbero.installations.model.server.Installation;
import com.at.cancerbero.installations.model.server.Installations;

import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InstallationRepository {

    private final BackEndClient serverConnector;

    public Set<Installation> loadInstallations() {
        return serverConnector.get("/installations", Installations.class, 200).getInstallations();
    }

    public Installation loadInstallation(UUID installationId) {
        return serverConnector.get("/installations/" + installationId, Installation.class, 200);
    }
}