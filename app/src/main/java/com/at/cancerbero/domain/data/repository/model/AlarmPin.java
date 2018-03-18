package com.at.cancerbero.domain.data.repository.model;

import com.at.cancerbero.domain.model.PinInput;
import com.at.cancerbero.domain.model.PinMode;
import com.at.cancerbero.domain.model.PinType;

import lombok.Data;

@Data
public class AlarmPin {
    private String id;
    private String name;
    private PinType type;
    private PinInput input;
    private PinMode mode;
    private String unit;
    private Float scale;
    private Integer threshold;
    private PinValue readings;
}