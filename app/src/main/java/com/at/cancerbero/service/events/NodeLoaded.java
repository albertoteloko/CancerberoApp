package com.at.cancerbero.service.events;

import com.at.cancerbero.installations.model.domain.Node;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class NodeLoaded implements Event {
    public final Node node;

    public NodeLoaded(Node node) {
        this.node = node;
    }
}
