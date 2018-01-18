package com.at.cancerbero.installations.async;

import android.util.Log;

import com.at.cancerbero.domain.model.Installation;
import com.at.cancerbero.service.async.AsyncGateway;
import com.at.cancerbero.service.events.Event;
import com.at.cancerbero.service.events.InstallationLoaded;

import java.util.UUID;


public class LoadInstallation extends LoadInstallationBase {

    private final UUID installationId;

    public LoadInstallation(AsyncGateway gateway, UUID installationId) {
        super(gateway);
        this.installationId = installationId;
    }

    @Override
    public Event run() {
        Installation serverInstallation = convertInstallation(asyncGateway.getiInstallationRepository().loadInstallation(installationId));
        Log.i(TAG, "Load node: " + serverInstallation);
        return new InstallationLoaded(serverInstallation);
    }
}
