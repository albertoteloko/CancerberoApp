package com.at.cancerbero.domain.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class AlarmStatusEvent {
    public final String source;
    public final Date timestamp;
    public final AlarmStatus value;
}
