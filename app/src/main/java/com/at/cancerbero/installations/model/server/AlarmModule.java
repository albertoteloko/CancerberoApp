package com.at.cancerbero.installations.model.server;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class AlarmModule {
    private AlarmStatusChangeEvent status;
    private Map<String, AlarmPin> pins = new HashMap<>();
}
