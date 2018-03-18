package com.at.hal9000.domain.service.push.model;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AlarmPinValueEvent extends Event {
    private String pinId;
    private String value;
}
