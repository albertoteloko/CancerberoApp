package com.at.cancerbero.domain.model;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class AlarmPinEvent {
    public final Date timestamp;
    public final int value;
}

