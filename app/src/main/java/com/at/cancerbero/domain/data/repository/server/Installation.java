package com.at.cancerbero.domain.data.repository.server;

import java.util.Set;
import java.util.UUID;

import lombok.Data;

@Data
public class Installation {
    private UUID id;
    private String name;
    private Set<String> users;
    private Set<String> nodes;
}
