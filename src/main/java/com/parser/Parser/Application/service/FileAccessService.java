package com.parser.Parser.Application.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch.core.IndexResponse;

import com.parser.Parser.Application.dto.NewScanEvent;
import com.parser.Parser.Application.dto.ParseAcknowledgement;
import com.parser.Parser.Application.model.AcknowledgementPayload;
import com.parser.Parser.Application.model.AcknowledgementStatus;
import com.parser.Parser.Application.model.FileLocationEvent;
import com.parser.Parser.Application.model.Finding;
import com.parser.Parser.Application.model.NewScanPayload;
import com.parser.Parser.Application.model.Tenant;
import com.parser.Parser.Application.model.ToolType;
import com.parser.Parser.Application.model.TriggerType;
import com.parser.Parser.Application.producer.AcknowledgementProducer;
import com.parser.Parser.Application.producer.NewScanProducer;
import com.parser.Parser.Application.repository.TenantRepository;
import com.parser.Parser.utils.FindingComparator;
import com.parser.Parser.utils.FindingHashCalculator;

@Service
public class FileAccessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileAccessService.class);

    private final ParserService parserService;
    private final ElasticsearchService elasticsearchService;
    private final TenantRepository tenantRepository;
    private final AcknowledgementProducer acknowledgementProducer;
    private final NewScanProducer newScanProducer;

    @Value("${app.kafka.topics.jfc-unified}")
    private String jfcUnifiedTopic;

    @Value("${app.kafka.topics.runbook-destination}")
    private String runbookDestinationTopic;

    public FileAccessService(ParserService parserService, 
                             ElasticsearchService elasticsearchService, 
                             TenantRepository tenantRepository,
                             AcknowledgementProducer acknowledgementProducer, NewScanProducer newScanProducer) {
        this.parserService = parserService;
        this.elasticsearchService = elasticsearchService;
        this.tenantRepository = tenantRepository;
        this.acknowledgementProducer = acknowledgementProducer;
        this.newScanProducer = newScanProducer;
    }
    
    public void processFile(FileLocationEvent fileDetails, String jobId){

        try {
            File file = new File(fileDetails.getFilePath());
            if (!file.exists()) {
                LOGGER.error("File not found at path: " + fileDetails.getFilePath());
                return;
            }
            String rawJson = Files.readString(file.toPath());
            ToolType toolType = fileDetails.getToolName();

            List<Finding> incomingFindings = parserService.parse(toolType, rawJson);

            Optional<Tenant> tenantOpt = tenantRepository.findByTenantId(fileDetails.getTenantId());
            if (tenantOpt.isEmpty()) {
                LOGGER.error("No tenant found for tenantId: {}", fileDetails.getTenantId());
                throw new RuntimeException("No tenant found for tenantId: " + fileDetails.getTenantId());
            }
            Tenant tenant = tenantOpt.get();
            String esIndex = tenant.getEsIndex();

            List<Finding> existingDocs = elasticsearchService.findByToolType(esIndex, toolType);

            Map<String, Finding> existingMap = new HashMap<>();
            for (Finding doc : existingDocs) {
                String docHash = FindingHashCalculator.computeHash(doc);
                existingMap.put(docHash, doc);
            }

            List<String> allFindingIds = new ArrayList<>();

            for (Finding f : incomingFindings) {
                String incomingHash = FindingHashCalculator.computeHash(f);

                if (!existingMap.containsKey(incomingHash)) {
                    IndexResponse response = elasticsearchService.indexFinding(esIndex, f);
                    allFindingIds.add(response.id());
                    LOGGER.info("Indexed doc ID: " + response.id() + " result: " + response.result());
                } else {
                    Finding existingDoc = existingMap.get(incomingHash);
                    if (FindingComparator.hasSignificantDifferences(existingDoc, f)) {
                        f.setId(existingDoc.getId());
                        f.setCreatedAt(existingDoc.getCreatedAt());
                        IndexResponse response = elasticsearchService.indexFinding(esIndex, f);
                        allFindingIds.add(f.getId());
                        LOGGER.info("Indexed doc ID: " + response.id() + " result: " + response.result());
                    } else {
                        allFindingIds.add(existingDoc.getId());
                        LOGGER.info("No changes => skipping doc => " + f.getId());
                    }
                }
            }

            NewScanPayload payload = new NewScanPayload(
                    fileDetails.getTenantId(),
                    jobId,
                    allFindingIds,
                    TriggerType.NEW_SCAN,
                    runbookDestinationTopic
            );

            NewScanEvent event = new NewScanEvent(payload);
            newScanProducer.publishNewScan(jfcUnifiedTopic, event);

            sendAcknowledgement(jobId, AcknowledgementStatus.SUCCESS);
        } catch (IOException e) {
            
            sendAcknowledgement(jobId, AcknowledgementStatus.FAILURE);
            e.printStackTrace();
        }
    }

    private void sendAcknowledgement(String jobId, AcknowledgementStatus status) {
        AcknowledgementPayload ackEvent = new AcknowledgementPayload(jobId);
        ackEvent.setStatus(status);
        ParseAcknowledgement parseAck = new ParseAcknowledgement(null, ackEvent);
        acknowledgementProducer.sendAcknowledgement(parseAck);
        LOGGER.info("Sent acknowledgement for jobId {}: {}", jobId, parseAck);
    }
}
