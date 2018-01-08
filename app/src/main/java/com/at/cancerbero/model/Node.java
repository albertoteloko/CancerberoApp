package com.at.cancerbero.model;

import java.util.Set;
import java.util.UUID;

import lombok.Data;

@Data
public class Node {
    private UUID id;
    private String name;
    private Set<String> users;
    private Set<String> nodes;
}
