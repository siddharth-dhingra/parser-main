package com.parser.Parser.Application.producer;

import com.parser.Parser.Application.dto.ParseAcknowledgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AcknowledgementProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcknowledgementProducer.class);

    @Value("${app.kafka.topics.job-acknowledgement}")
    private String acknowledgementTopic;

    private final KafkaTemplate<String, ParseAcknowledgement> kafkaTemplate;

    public AcknowledgementProducer(KafkaTemplate<String, ParseAcknowledgement> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAcknowledgement(ParseAcknowledgement ack) {
        kafkaTemplate.send(acknowledgementTopic, ack);
        LOGGER.info("Published ParseAcknowledgement to topic {} => {}", acknowledgementTopic, ack);
    }
}