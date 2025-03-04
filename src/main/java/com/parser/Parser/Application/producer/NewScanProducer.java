package com.parser.Parser.Application.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parser.Parser.Application.dto.NewScanEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NewScanProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewScanProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public NewScanProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishNewScan(String topic, NewScanEvent event) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, jsonMessage);
            LOGGER.info("Published NewScanEvent to topic {}: {}", topic, jsonMessage);
        } catch (Exception e) {
            LOGGER.error("Error publishing NewScanEvent: ", e);
        }
    }
}