package com.parser.Parser.Application.dto;

import java.util.UUID;

import com.parser.Parser.Application.model.Event;
import com.parser.Parser.Application.model.EventTypes;
import com.parser.Parser.Application.model.NewScanPayload;

public class NewScanEvent implements Event<NewScanPayload> {

    private String eventId;
    public static final EventTypes TYPE = EventTypes.RUNBOOK;
    private NewScanPayload payload;

    public NewScanEvent() {}

    public NewScanEvent(NewScanPayload payload) {
        this.eventId = (payload.getJobId() == null) ? UUID.randomUUID().toString() : payload.getJobId();
        this.payload = payload;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public EventTypes getType() {
        return TYPE;
    }

    @Override
    public NewScanPayload getPayload() {
        return payload;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setPayload(NewScanPayload payload) {
        this.payload = payload;
    }
}