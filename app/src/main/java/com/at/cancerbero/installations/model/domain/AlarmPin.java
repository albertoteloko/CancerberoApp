package com.at.cancerbero.installations.model.domain;

import com.at.cancerbero.installations.model.common.PinInput;
import com.at.cancerbero.installations.model.common.PinMode;
import com.at.cancerbero.installations.model.common.PinType;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class AlarmPin {
    public final String id;
    public final PinType type;
    public final PinInput input;
    public final PinMode mode;
    public final int threshold;
    public final AlarmPinChangeEvent activations;
    public final AlarmPinChangeEvent readings;
}