package com.at.cancerbero.domain.data.repository;

import com.at.cancerbero.domain.data.repository.server.Installation;
import com.at.cancerbero.domain.data.repository.server.Installations;

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