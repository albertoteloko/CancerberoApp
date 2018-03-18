package com.at.cancerbero.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class AlarmPin {
    public final String id;
    public final String name;
    public final PinType type;
    public final PinInput input;
    public final PinMode mode;
    public final String unit;
    public final Float scale;
    public final Integer threshold;
    public PinValue readings;
}