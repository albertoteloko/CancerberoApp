package com.at.hal9000.domain.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class AlarmModule {
    public AlarmStatusEvent status;
    public final Map<String, AlarmPin> pins;
}
