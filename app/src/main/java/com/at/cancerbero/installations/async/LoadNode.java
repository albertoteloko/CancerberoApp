package com.at.cancerbero.installations.async;

import android.util.Log;

import com.at.cancerbero.domain.model.domain.Node;
import com.at.cancerbero.service.async.AsyncGateway;
import com.at.cancerbero.service.events.Event;
import com.at.cancerbero.service.events.NodeLoaded;


public class LoadNode extends LoadInstallationBase {

    private final String nodeId;

    public LoadNode(AsyncGateway gateway, String nodeId) {
        super(gateway);
        this.nodeId = nodeId;
    }

    @Override
    public Event run() {
        Node serverInstallation = convertNode(asyncGateway.getNodesRepository().loadNode(nodeId));
        Log.i(TAG, "Load node: " + serverInstallation);
        return new NodeLoaded(serverInstallation);
    }
}
