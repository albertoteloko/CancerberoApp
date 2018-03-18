package com.at.cancerbero.domain.data.repository.model;

import java.util.Date;
import java.util.UUID;

import lombok.Data;

@Data
public class PinValue {
    private Date timestamp;
    private int value;
}

