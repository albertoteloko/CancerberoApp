package com.at.cancerbero.installations.model.domain;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class AlarmPinChangeEvent {
    public final UUID id;
    public final Date timestamp;
    public final int value;
}

