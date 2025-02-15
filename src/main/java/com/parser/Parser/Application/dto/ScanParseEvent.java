package com.parser.Parser.Application.dto;

import java.util.UUID;

import com.parser.Parser.Application.model.Event;
import com.parser.Parser.Application.model.EventTypes;
import com.parser.Parser.Application.model.FileLocationEvent;

public class ScanParseEvent implements Event<FileLocationEvent> {
    
    private String eventId;
    public static EventTypes TYPE = EventTypes.SCAN_PARSE;
    private FileLocationEvent payload;

    public ScanParseEvent() {}

    public ScanParseEvent(String eventId, FileLocationEvent payload) {
        this.payload = payload;
        this.eventId = (eventId == null || eventId.isEmpty()) ? UUID.randomUUID().toString() : eventId;
    }

    public static EventTypes getTYPE() {
        return TYPE;
    }

    public static void setTYPE(EventTypes tYPE) {
        TYPE = tYPE;
    }

    public void setPayload(FileLocationEvent payload) {
        this.payload = payload;
    }

    @Override
    public EventTypes getType() {
        return TYPE;
    }

    @Override
    public FileLocationEvent getPayload() {
        return payload;
    }

    @Override
    public String getEventId() {
        return eventId;
    }
}