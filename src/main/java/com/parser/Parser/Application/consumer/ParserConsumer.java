package com.parser.Parser.Application.consumer;

import com.parser.Parser.Application.dto.ScanParseEvent;
import com.parser.Parser.Application.model.FileLocationEvent;
import com.parser.Parser.Application.service.FileAccessService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ParserConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParserConsumer.class);

    private final FileAccessService fileAccessService;

    @Value("${app.kafka.topics.filelocation}")
    private String fileLocationTopic;

    public ParserConsumer(FileAccessService fileAccessService) {
        this.fileAccessService = fileAccessService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.filelocation}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "fileLocationEventListenerFactory"
    )
    public void consumeFileLocationEvent(ConsumerRecord<String, ScanParseEvent> record) {
        ScanParseEvent eventWrapper = record.value();
        FileLocationEvent fle = eventWrapper.getPayload();
        String jobId = fle.getJobId();
        LOGGER.info("Received FileLocationEvent: " + fle);

        fileAccessService.processFile(fle, jobId);
    }
}
