package com.at.hal9000.domain.data.repository.model;

import com.at.hal9000.domain.model.NodeType;

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
