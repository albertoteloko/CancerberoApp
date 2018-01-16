package com.at.cancerbero.domain.data.repository.server;

import com.at.cancerbero.domain.model.common.PinInput;
import com.at.cancerbero.domain.model.common.PinMode;
import com.at.cancerbero.domain.model.common.PinType;

import lombok.Data;

@Data
public class AlarmPin {
    private String id;
    private PinType type;
    private PinInput input;
    private PinMode mode;
    private int threshold;
    private AlarmPinChangeEvent activations;
    private AlarmPinChangeEvent readings;
}