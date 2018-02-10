package com.at.cancerbero.domain.data.repository.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AddCard {
    public final String type = "addCard";
    public final String cardId;
    public final String name;
}
