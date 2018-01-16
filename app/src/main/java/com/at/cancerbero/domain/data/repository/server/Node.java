package com.at.cancerbero.domain.data.repository.server;

import com.at.cancerbero.domain.model.common.NodeType;

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
