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
import com.at.cancerbero.service.events.InstallationLoaded;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class LoadInstallations extends ServiceAsyncTask {
    public LoadInstallations(AsyncGateway gateway) {
        super(gateway);
    }

    @Override
    public Event run() {
        Set<Installation> serverInstallation = convertInstallations(asyncGateway.getiInstallationRepository().loadInstallations());
        Log.i(TAG, "My installation: " + serverInstallation);
        return new InstallationLoaded(serverInstallation);
    }

    private Set<Installation> convertInstallations(Set<com.at.cancerbero.installations.model.server.Installation> input) {
        Set<Installation> result = new HashSet<>();

        for (com.at.cancerbero.installations.model.server.Installation installation : input) {
            result.add(convertInstallation(installation));
        }

        return result;
    }

    private Installation convertInstallation(com.at.cancerbero.installations.model.server.Installation input) {
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
            result.add(convertNode(asyncGateway.getNodesRepository().loadNode(nodeId)));
        }

        return result;
    }

    private Node convertNode(com.at.cancerbero.installations.model.server.Node input) {
        return new Node(
                input.getId(),
                input.getName(),
                input.getType(),
                input.getLastPing(),
                convertModules(input.getModules())
        );
    }

    private NodeModules convertModules(com.at.cancerbero.installations.model.server.NodeModules input) {
        return new NodeModules(
                convertAlarmModule(input.getAlarm())
        );
    }

    private AlarmModule convertAlarmModule(com.at.cancerbero.installations.model.server.AlarmModule input) {
        return new AlarmModule(
                convert(input.getStatus()),
                convertPins(input.getPins())
        );
    }

    private Map<String, AlarmPin> convertPins(Map<String, com.at.cancerbero.installations.model.server.AlarmPin> input) {
        Map<String, AlarmPin> result = new HashMap<>();

        for (String pinId : input.keySet()) {
            result.put(pinId, convertPin(input.get(pinId)));
        }

        return result;
    }

    private AlarmPin convertPin(com.at.cancerbero.installations.model.server.AlarmPin input) {
        return new AlarmPin(
                input.getId(),
                input.getType(),
                input.getInput(),
                input.getMode(),
                input.getThreshold(),
                convert(input.getActivations()),
                convert(input.getReadings())
        );
    }

    private AlarmPinChangeEvent convert(com.at.cancerbero.installations.model.server.AlarmPinChangeEvent input) {
        return new AlarmPinChangeEvent(
                input.getId(),
                input.getTimestamp(),
                input.getValue()
        );
    }

    private AlarmStatusChangeEvent convert(com.at.cancerbero.installations.model.server.AlarmStatusChangeEvent input) {
        return new AlarmStatusChangeEvent(
                input.getId(),
                input.getSource(),
                input.getTimestamp(),
                input.getValue()
        );
    }
}
