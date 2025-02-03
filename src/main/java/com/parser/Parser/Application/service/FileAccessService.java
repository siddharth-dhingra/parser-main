package com.parser.Parser.Application.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch.core.IndexResponse;


import com.parser.Parser.Application.model.FileLocationEvent;
import com.parser.Parser.Application.model.Finding;
import com.parser.Parser.Application.model.ToolType;

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

            List<Finding> findings = parserService.parse(toolType, rawJson);

            for (Finding f : findings) {
                IndexResponse response = elasticsearchService.indexFinding(f);
                LOGGER.info("Indexed doc ID: " + response.id() + " result: " + response.result());;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ToolType mapToolType(String toolName) {
        if (toolName == null) {
            return ToolType.CODESCAN; // fallback
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
