package com.parser.Parser.Application.model;

public interface Event<T> {
    String getEventId();
    EventTypes getType();
    T getPayload();
}