package com.at.hal9000.domain.data.repository.model;

import com.at.hal9000.domain.model.AlarmStatus;

import java.util.Date;

import lombok.Data;

@Data
public class AlarmStatusChangeEvent {
    private String source;
    private String sourceName;
    private Date timestamp;
    private AlarmStatus value;
}
