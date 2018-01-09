package com.at.cancerbero.installations.model.domain;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class AlarmModule {
    public final AlarmStatusChangeEvent status;
    public final Map<String, AlarmPin> pins;
}
