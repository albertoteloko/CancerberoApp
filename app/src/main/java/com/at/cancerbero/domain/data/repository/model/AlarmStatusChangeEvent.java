package com.at.cancerbero.domain.data.repository.model;

import com.at.cancerbero.domain.model.AlarmStatus;

import java.util.Date;
import java.util.UUID;

import lombok.Data;

@Data
public class AlarmStatusChangeEvent {
    private String source;
    private String sourceName;
    private Date timestamp;
    private AlarmStatus value;
}
