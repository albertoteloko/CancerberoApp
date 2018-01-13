package com.at.cancerbero.installations.async;

import android.util.Log;

import com.at.cancerbero.installations.model.domain.AlarmModule;
import com.at.cancerbero.installations.model.domain.AlarmPin;
import com.at.cancerbero.installations.model.domain.AlarmPinChangeEvent;
import com.at.cancerbero.installations.model.domain.AlarmStatusChangeEvent;
import com.at.cancerbero.installations.model.domain.Installation;
import com.at.cancerbero.installations.model.domain.Node;
import com.at.cancerbero.installations.model.domain.NodeModules;
import com.at.cancerbero.service.async.AsyncGateway;
import com.at.cancerbero.service.async.ServiceAsyncTask;
import com.at.cancerbero.service.events.Event;
import com.at.cancerbero.service.events.InstallationsLoaded;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    private Set<Installation> convertInstallations(Set<com.at.cancerbero.installations.model.server.Installation> input) {
        Set<Installation> result = new HashSet<>();

        for (com.at.cancerbero.installations.model.server.Installation installation : input) {
            result.add(convertInstallation(installation));
        }

        return result;
    }
}
