package com.at.hal9000.domain.service.push.model;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Date;
import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AlarmPinValueEvent.class, name = "alarm-pin-value"),
        @JsonSubTypes.Type(value = AlarmStatusChanged.class, name = "alarm-status-changed"),
})
@Data
@ToString
@EqualsAndHashCode
public class Event {
    private UUID id;
    private String nodeId;
    private Date timestamp;
}
