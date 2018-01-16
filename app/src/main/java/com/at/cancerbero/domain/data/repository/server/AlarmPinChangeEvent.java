package com.at.cancerbero.domain.data.repository.server;

import java.util.Date;
import java.util.UUID;

import lombok.Data;

@Data
public class AlarmPinChangeEvent {
    private UUID id;
    private Date timestamp;
    private int value;
}

