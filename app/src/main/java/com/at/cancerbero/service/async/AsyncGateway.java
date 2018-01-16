package com.at.cancerbero.service.async;

import com.at.cancerbero.domain.data.repository.InstallationRepository;
import com.at.cancerbero.domain.data.repository.NodesRepository;
import com.at.cancerbero.service.events.Event;

public interface AsyncGateway {

    NodesRepository getNodesRepository();

    InstallationRepository getiInstallationRepository();

    void sendEvent(Event event);
}
