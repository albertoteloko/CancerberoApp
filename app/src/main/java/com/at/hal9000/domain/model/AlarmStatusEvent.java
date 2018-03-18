package com.at.hal9000.domain.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class AlarmStatusEvent {
    public final String source;
    public final String sourceName;
    public final Date timestamp;
    public final AlarmStatus value;
}
