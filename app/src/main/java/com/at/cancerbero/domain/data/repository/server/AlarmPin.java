package com.at.cancerbero.domain.data.repository.server;

import com.at.cancerbero.domain.model.PinInput;
import com.at.cancerbero.domain.model.PinMode;
import com.at.cancerbero.domain.model.PinType;

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