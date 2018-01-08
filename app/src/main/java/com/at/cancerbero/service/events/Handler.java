package com.at.cancerbero.service.events;

public interface Handler {
    boolean handle(Event event);
}
