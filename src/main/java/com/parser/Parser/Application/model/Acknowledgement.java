package com.parser.Parser.Application.model;

public interface Acknowledgement<T> {
    String getAcknowledgementId();
    T getPayload();
}