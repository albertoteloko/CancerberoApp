package com.at.hal9000.domain.data.repository.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RemoveCard {
    public final String type = "removeCard";
    public final String cardId;
}
