package com.at.cancerbero.installations.model.domain;

import com.at.cancerbero.installations.model.common.NodeType;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Node {
    public final String id;
    public final String name;
    public final NodeType type;
    public final Date lastPing;
    public final NodeModules modules;
}
