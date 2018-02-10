package com.at.cancerbero.domain.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class CardModule {
    public final String spi;
    public final String ss;
    public final Map<String, String> entries;
}
