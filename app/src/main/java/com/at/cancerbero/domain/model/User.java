package com.at.cancerbero.domain.model;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@AllArgsConstructor
public class User {
    private final String userId;
    private final String name;
    private final String token;
    private final Set<String> groups;
}
