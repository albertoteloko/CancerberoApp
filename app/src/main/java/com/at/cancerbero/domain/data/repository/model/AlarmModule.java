package com.at.cancerbero.domain.data.repository.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class AlarmModule {
    private AlarmStatusChangeEvent status;
    private Map<String, AlarmPin> pins = new HashMap<>();
}
