package com.at.cancerbero.domain.model.domain;

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
    private final Set<String> groups;
}
