package com.at.hal9000.domain.data.repository.model;

import com.at.hal9000.domain.model.PinInput;
import com.at.hal9000.domain.model.PinMode;
import com.at.hal9000.domain.model.PinType;

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