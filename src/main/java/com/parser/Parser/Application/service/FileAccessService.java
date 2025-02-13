package com.parser.Parser.Application.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch.core.IndexResponse;

import com.parser.Parser.Application.model.FileLocationEvent;
import com.parser.Parser.Application.model.Finding;
import com.parser.Parser.Application.model.ToolType;
import com.parser.Parser.utils.FindingComparator;
import com.parser.Parser.utils.FindingHashCalculator;

@Service
public class FileAccessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileAccessService.class);

    private final ParserService parserService;
    private final ElasticsearchService elasticsearchService;

    public FileAccessService(ParserService parserService, ElasticsearchService elasticsearchService) {
        this.parserService = parserService;
        this.elasticsearchService = elasticsearchService;
    }
    
    public void processFile(FileLocationEvent fileDetails){

        File file = new File(fileDetails.getFilePath());
        if (!file.exists()) {
            LOGGER.error("File not found at path: " + fileDetails.getFilePath());
            return;
        }

        try {
            String rawJson = Files.readString(file.toPath());
            ToolType toolType = mapToolType(fileDetails.getToolName());

            List<Finding> incomingFindings = parserService.parse(toolType, rawJson);

            List<Finding> existingDocs = elasticsearchService.findByToolType(fileDetails.getEsIndex(), toolType);

            Map<String, Finding> existingMap = new HashMap<>();
            for (Finding doc : existingDocs) {
                String docHash = FindingHashCalculator.computeHash(doc);
                existingMap.put(docHash, doc);
            }

            for (Finding f : incomingFindings) {
                String incomingHash = FindingHashCalculator.computeHash(f);

                if (!existingMap.containsKey(incomingHash)) {
                    IndexResponse response = elasticsearchService.indexFinding(fileDetails.getEsIndex(), f);
                    LOGGER.info("Indexed doc ID: " + response.id() + " result: " + response.result());
                } else {
                    Finding existingDoc = existingMap.get(incomingHash);
                    if (FindingComparator.hasSignificantDifferences(existingDoc, f)) {
                        f.setId(existingDoc.getId());
                        f.setCreatedAt(existingDoc.getCreatedAt());
                        IndexResponse response = elasticsearchService.indexFinding(fileDetails.getEsIndex(), f);
                        LOGGER.info("Indexed doc ID: " + response.id() + " result: " + response.result());
                    } else {
                        LOGGER.info("No changes => skipping doc => " + f.getId());
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ToolType mapToolType(String toolName) {
        if (toolName == null) {
            return ToolType.CODESCAN;
        }
        switch (toolName.toLowerCase()) {
            case "codescan":
                return ToolType.CODESCAN;
            case "dependabot":
                return ToolType.DEPENDABOT;
            case "secretscan":
                return ToolType.SECRETSCAN;
            default:
                return ToolType.CODESCAN;
        }
    }
}
