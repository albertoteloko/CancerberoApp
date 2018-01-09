package com.at.cancerbero.installations.model.domain;

import com.at.cancerbero.installations.model.common.AlarmStatus;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class AlarmStatusChangeEvent {
    public final UUID id;
    public final String source;
    public final Date timestamp;
    public final AlarmStatus status;
}
