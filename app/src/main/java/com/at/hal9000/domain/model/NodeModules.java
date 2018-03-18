package com.at.hal9000.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class NodeModules {
    public final AlarmModule alarm;
    public final CardModule card;
}
