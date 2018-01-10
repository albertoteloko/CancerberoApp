package com.at.cancerbero.installations.model.server;

import com.at.cancerbero.installations.model.common.AlarmStatus;

import java.util.Date;
import java.util.UUID;

import lombok.Data;

@Data
public class AlarmStatusChangeEvent {
    private UUID id;
    private String source;
    private Date timestamp;
    private AlarmStatus value;
}
