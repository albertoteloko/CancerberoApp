package com.at.cancerbero.installations.model.server;

import com.at.cancerbero.installations.model.common.NodeType;

import java.util.Date;

import lombok.Data;

@Data
public class Node {
    private String id;
    private String name;
    private NodeType type;
    private Date lastPing;
    private NodeModules modules;
}
