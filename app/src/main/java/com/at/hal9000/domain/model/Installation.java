package com.at.hal9000.domain.model;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Installation {
    public final UUID id;
    public final String name;
    public final Set<String> users;
    public final Set<Node> nodes;

    public Installation(UUID id, String name, Set<String> users, Set<Node> nodes) {
        this.id = id;
        this.name = name;
        this.users = Collections.unmodifiableSet(users);
        this.nodes = Collections.unmodifiableSet(nodes);
    }

    public AlarmStatus getAlarmStatus() {
        AlarmStatus status = AlarmStatus.IDLE;

        for (Node node : nodes) {
            if((node.modules.alarm != null) && (node.modules.alarm.status != null)){
                if(node.modules.alarm.status.value.ordinal() > status.ordinal()){
                    status = node.modules.alarm.status.value;
                }
            }
        }


        return status;
    }
}
