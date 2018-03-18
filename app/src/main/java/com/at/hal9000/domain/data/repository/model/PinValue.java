package com.at.hal9000.domain.data.repository.model;

import java.util.Date;

import lombok.Data;

@Data
public class PinValue {
    private Date timestamp;
    private int value;
}

