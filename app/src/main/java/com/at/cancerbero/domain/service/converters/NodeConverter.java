package com.at.cancerbero.domain.service.converters;


import com.at.cancerbero.domain.model.AlarmModule;
import com.at.cancerbero.domain.model.AlarmPin;
import com.at.cancerbero.domain.model.AlarmPinChangeEvent;
import com.at.cancerbero.domain.model.AlarmStatusChangeEvent;
import com.at.cancerbero.domain.model.Node;
import com.at.cancerbero.domain.model.NodeModules;

import java.util.HashMap;
import java.util.Map;

public class NodeConverter {
    public Node convert(com.at.cancerbero.domain.data.repository.server.Node input) {
        return new Node(
                input.getId(),
                input.getName(),
                input.getType(),
                input.getLastPing(),
                convertModules(input.getModules())
        );
    }

    private NodeModules convertModules(com.at.cancerbero.domain.data.repository.server.NodeModules input) {
        return new NodeModules(
                convertAlarmModule(input.getAlarm())
        );
    }

    private AlarmModule convertAlarmModule(com.at.cancerbero.domain.data.repository.server.AlarmModule input) {
        return new AlarmModule(
                convert(input.getStatus()),
                convertPins(input.getPins())
        );
    }

    private Map<String, AlarmPin> convertPins(Map<String, com.at.cancerbero.domain.data.repository.server.AlarmPin> input) {
        Map<String, AlarmPin> result = new HashMap<>();

        for (String pinId : input.keySet()) {
            result.put(pinId, convertPin(input.get(pinId)));
        }

        return result;
    }

    private AlarmPin convertPin(com.at.cancerbero.domain.data.repository.server.AlarmPin input) {
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

    private AlarmPinChangeEvent convert(com.at.cancerbero.domain.data.repository.server.AlarmPinChangeEvent input) {
        return new AlarmPinChangeEvent(
                input.getId(),
                input.getTimestamp(),
                input.getValue()
        );
    }

    private AlarmStatusChangeEvent convert(com.at.cancerbero.domain.data.repository.server.AlarmStatusChangeEvent input) {
        return new AlarmStatusChangeEvent(
                input.getId(),
                input.getSource(),
                input.getTimestamp(),
                input.getValue()
        );
    }
}
