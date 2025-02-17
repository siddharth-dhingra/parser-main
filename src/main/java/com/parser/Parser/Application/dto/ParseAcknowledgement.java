package com.parser.Parser.Application.dto;

import java.util.UUID;

import com.parser.Parser.Application.model.Acknowledgement;
import com.parser.Parser.Application.model.AcknowledgementPayload;

public class ParseAcknowledgement implements Acknowledgement<AcknowledgementPayload> {
    
    private String acknowledgementId;
    private AcknowledgementPayload payload;

    public ParseAcknowledgement() {}   

    public ParseAcknowledgement(String acknowledgementId, AcknowledgementPayload payload) {
        this.acknowledgementId = (acknowledgementId == null || acknowledgementId.isEmpty()) ? UUID.randomUUID().toString() : acknowledgementId;
        this.payload = payload;
    }

    public void setAcknowledgementId(String acknowledgementId) {
        this.acknowledgementId = acknowledgementId;
    }

    public void setPayload(AcknowledgementPayload payload) {
        this.payload = payload;
    }

    @Override
    public String getAcknowledgementId() {
        return acknowledgementId;
    }

    @Override
    public AcknowledgementPayload getPayload() {
        return payload;
    }
}
