package com.at.cancerbero.domain.data.repository.model;

import com.at.cancerbero.domain.model.AlarmStatus;

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
