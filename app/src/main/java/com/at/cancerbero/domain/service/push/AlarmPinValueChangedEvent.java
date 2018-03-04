package com.at.cancerbero.domain.service.push;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AlarmPinValueChangedEvent extends Event {
    private String pinId;
    private String value;
}
