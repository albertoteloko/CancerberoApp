package com.at.cancerbero.domain.service.push;


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
        @JsonSubTypes.Type(value = AlarmPinActivatedEvent.class, name = "alarm-pin-activated"),
        @JsonSubTypes.Type(value = AlarmPinValueChangedEvent.class, name = "alarm-pin-changed"),
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
