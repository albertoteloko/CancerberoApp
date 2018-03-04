package com.at.cancerbero.domain.service.push.model;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AlarmPinActivatedEvent extends Event {
    private String pinId;
    private String value;
}
