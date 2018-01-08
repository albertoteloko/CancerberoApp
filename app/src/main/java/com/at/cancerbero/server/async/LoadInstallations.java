package com.at.cancerbero.server.async;

import android.util.Log;

import com.at.cancerbero.model.Installation;
import com.at.cancerbero.model.Node;
import com.at.cancerbero.service.events.Event;
import com.at.cancerbero.service.events.InstallationLoaded;

import java.util.HashSet;
import java.util.Set;


public class LoadInstallations extends ServiceAsyncTask {
    public LoadInstallations(AsyncGateway gateway) {
        super(gateway);
    }

    @Override
    public Event run() {
        Set<Installation> serverInstallation = convertInstallations(asyncGateway.getServerClient().loadInstallations());
        Log.i(TAG, "My installation: " + serverInstallation);
        return new InstallationLoaded(serverInstallation);
    }

    private Set<Installation> convertInstallations(Set<com.at.cancerbero.server.model.Installation> input) {
        Set<Installation> result = new HashSet<>();

        for (com.at.cancerbero.server.model.Installation installation : input) {
            result.add(convertInstallation(installation));
        }

        return result;
    }

    private Installation convertInstallation(com.at.cancerbero.server.model.Installation input) {
        return new Installation(
                input.getId(),
                input.getName(),
                input.getUsers(),
                loadAndConvertNodes(input.getNodes())
        );
    }

    private Set<Node> loadAndConvertNodes(Set<String> input) {
        Set<Node> result = new HashSet<>();

        for (String nodeId : input) {
            result.add(convertNode(asyncGateway.getServerClient().loadNode(nodeId)));
        }

        return result;
    }

    private Node convertNode(com.at.cancerbero.server.model.Node input) {
        return new Node(

        );
    }
}
