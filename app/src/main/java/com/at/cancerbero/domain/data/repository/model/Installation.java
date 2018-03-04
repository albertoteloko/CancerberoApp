package com.at.cancerbero.domain.data.repository.model;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.Data;

@Data
public class Installation {
    private UUID id;
    private String name;
    private Set<String> users;
    private List<Node> nodes;
}
