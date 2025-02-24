package com.parser.Parser.Application.consumer;

import com.parser.Parser.Application.dto.ScanParseEvent;
import com.parser.Parser.Application.model.FileLocationEvent;
import com.parser.Parser.Application.service.FileAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ParserConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParserConsumer.class);

    private final FileAccessService fileAccessService;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.filelocation}")
    private String fileLocationTopic;

    public ParserConsumer(FileAccessService fileAccessService, ObjectMapper objectMapper) {
        this.fileAccessService = fileAccessService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.filelocation}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "stringListenerContainerFactory"
    )
    public void consumeFileLocationEvent(String message) {

        try {
            ScanParseEvent eventWrapper = objectMapper.readValue(message, ScanParseEvent.class);
            FileLocationEvent fle = eventWrapper.getPayload();
            String jobId = fle.getJobId();
            
            LOGGER.info("Received FileLocationEvent: " + fle);

            fileAccessService.processFile(fle, jobId);
        } catch (Exception e) {
            LOGGER.error("Error processing file location event message: {}", message, e);
        }
    }
}
