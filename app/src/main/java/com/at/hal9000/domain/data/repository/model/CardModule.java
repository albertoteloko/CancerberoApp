package com.at.hal9000.domain.data.repository.model;

import java.util.Map;

import lombok.Data;

@Data
public class CardModule {
    private String spi;
    private String ss;
    private Map<String, String> entries;
}
