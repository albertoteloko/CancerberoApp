package com.at.cancerbero.installations.async;

import com.at.cancerbero.installations.model.domain.AlarmModule;
import com.at.cancerbero.installations.model.domain.AlarmPin;
import com.at.cancerbero.installations.model.domain.AlarmPinChangeEvent;
import com.at.cancerbero.installations.model.domain.AlarmStatusChangeEvent;
import com.at.cancerbero.installations.model.domain.Installation;
import com.at.cancerbero.installations.model.domain.Node;
import com.at.cancerbero.installations.model.domain.NodeModules;
import com.at.cancerbero.service.async.AsyncGateway;
import com.at.cancerbero.service.async.ServiceAsyncTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public abstract class LoadInstallationBase extends ServiceAsyncTask {

    public LoadInstallationBase(AsyncGateway asyncGateway) {
        super(asyncGateway);
    }

    protected Installation convertInstallation(com.at.cancerbero.installations.model.server.Installation input) {
        return new Installation(
                input.getId(),
                input.getName(),
                input.getUsers(),
                loadAndConvertNodes(input.getNodes())
        );
    }

    protected Set<Node> loadAndConvertNodes(Set<String> input) {
        Set<Node> result = new HashSet<>();

        for (String nodeId : input) {
            result.add(convertNode(asyncGateway.getNodesRepository().loadNode(nodeId)));
        }

        return result;
    }

    protected Node convertNode(com.at.cancerbero.installations.model.server.Node input) {
        return new Node(
                input.getId(),
                input.getName(),
                input.getType(),
                input.getLastPing(),
                convertModules(input.getModules())
        );
    }

    protected NodeModules convertModules(com.at.cancerbero.installations.model.server.NodeModules input) {
        return new NodeModules(
                convertAlarmModule(input.getAlarm())
        );
    }

    protected AlarmModule convertAlarmModule(com.at.cancerbero.installations.model.server.AlarmModule input) {
        return new AlarmModule(
                convert(input.getStatus()),
                convertPins(input.getPins())
        );
    }

    protected Map<String, AlarmPin> convertPins(Map<String, com.at.cancerbero.installations.model.server.AlarmPin> input) {
        Map<String, AlarmPin> result = new HashMap<>();

        for (String pinId : input.keySet()) {
            result.put(pinId, convertPin(input.get(pinId)));
        }

        return result;
    }

    protected AlarmPin convertPin(com.at.cancerbero.installations.model.server.AlarmPin input) {
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

    protected AlarmPinChangeEvent convert(com.at.cancerbero.installations.model.server.AlarmPinChangeEvent input) {
        return new AlarmPinChangeEvent(
                input.getId(),
                input.getTimestamp(),
                input.getValue()
        );
    }

    protected AlarmStatusChangeEvent convert(com.at.cancerbero.installations.model.server.AlarmStatusChangeEvent input) {
        return new AlarmStatusChangeEvent(
                input.getId(),
                input.getSource(),
                input.getTimestamp(),
                input.getValue()
        );
    }
}
