package com.at.cancerbero.domain.service.converters;


import com.at.cancerbero.domain.model.AlarmModule;
import com.at.cancerbero.domain.model.AlarmPin;
import com.at.cancerbero.domain.model.PinValue;
import com.at.cancerbero.domain.model.AlarmStatusEvent;
import com.at.cancerbero.domain.model.CardModule;
import com.at.cancerbero.domain.model.Node;
import com.at.cancerbero.domain.model.NodeModules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class NodeConverter {

    public List<Node> convert(List<com.at.cancerbero.domain.data.repository.model.Node> input) {
        return StreamSupport.stream(input).map(this::convert).collect(Collectors.toList());
    }

    public Node convert(com.at.cancerbero.domain.data.repository.model.Node input) {
        Node result = null;

        if (input != null) {
            result = new Node(
                    input.getId(),
                    input.getName(),
                    input.getType(),
                    input.getLastPing(),
                    convertModules(input.getModules())
            );
        }
        return result;
    }

    private NodeModules convertModules(com.at.cancerbero.domain.data.repository.model.NodeModules input) {
        return new NodeModules(
                convertAlarmModule(input.getAlarm()),
                convertCardModule(input.getCard())
        );
    }

    private AlarmModule convertAlarmModule(com.at.cancerbero.domain.data.repository.model.AlarmModule input) {
        return new AlarmModule(
                convert(input.getStatus()),
                convertPins(input.getPins())
        );
    }

    private CardModule convertCardModule(com.at.cancerbero.domain.data.repository.model.CardModule input) {
        return new CardModule(
                input.getSpi(),
                input.getSs(),
                new HashMap<>(input.getEntries())
        );
    }

    private Map<String, AlarmPin> convertPins(Map<String, com.at.cancerbero.domain.data.repository.model.AlarmPin> input) {
        Map<String, AlarmPin> result = new HashMap<>();

        for (String pinId : input.keySet()) {
            result.put(pinId, convertPin(input.get(pinId)));
        }

        return result;
    }

    private AlarmPin convertPin(com.at.cancerbero.domain.data.repository.model.AlarmPin input) {
        return new AlarmPin(
                input.getId(),
                input.getName(),
                input.getType(),
                input.getInput(),
                input.getMode(),
                input.getUnit(),
                input.getScale(),
                input.getThreshold(),
                convert(input.getReadings())
        );
    }

    private PinValue convert(com.at.cancerbero.domain.data.repository.model.PinValue input) {
        return new PinValue(
                input.getTimestamp(),
                input.getValue()
        );
    }

    private AlarmStatusEvent convert(com.at.cancerbero.domain.data.repository.model.AlarmStatusChangeEvent input) {
        return new AlarmStatusEvent(
                input.getSource(),
                input.getSourceName(),
                input.getTimestamp(),
                input.getValue()
        );
    }
}
