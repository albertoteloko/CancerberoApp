package com.at.cancerbero.domain.data.repository.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RemoveCard {
    public final String type = "removeCard";
    public final String cardId;
}
