package com.at.cancerbero.server.async;

import com.at.cancerbero.server.APIGatewayServerClient;
import com.at.cancerbero.service.events.Event;

public interface AsyncGateway {

    APIGatewayServerClient getServerClient();

    void sendEvent(Event event);
}
