package com.at.cancerbero.installations.async;

import android.util.Log;

import com.at.cancerbero.domain.model.domain.Installation;
import com.at.cancerbero.service.async.AsyncGateway;
import com.at.cancerbero.service.events.Event;
import com.at.cancerbero.service.events.InstallationsLoaded;

import java.util.HashSet;
import java.util.Set;


public class LoadInstallations extends LoadInstallationBase {
    public LoadInstallations(AsyncGateway gateway) {
        super(gateway);
    }

    @Override
    public Event run() {
        Set<Installation> serverInstallation = convertInstallations(asyncGateway.getiInstallationRepository().loadInstallations());
        Log.i(TAG, "Load installations: " + serverInstallation);
        return new InstallationsLoaded(serverInstallation);
    }

    private Set<Installation> convertInstallations(Set<com.at.cancerbero.domain.data.repository.server.Installation> input) {
        Set<Installation> result = new HashSet<>();

        for (com.at.cancerbero.domain.data.repository.server.Installation installation : input) {
            result.add(convertInstallation(installation));
        }

        return result;
    }
}
