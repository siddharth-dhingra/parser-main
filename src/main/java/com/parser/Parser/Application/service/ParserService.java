package com.parser.Parser.Application.service;

import com.parser.Parser.Application.model.Finding;
import com.parser.Parser.Application.model.ToolType;
import com.parser.Parser.Application.service.toolParser.CodeScanParser;
import com.parser.Parser.Application.service.toolParser.DependabotParser;
import com.parser.Parser.Application.service.toolParser.SecretScanParser;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ParserService {

    // private final ObjectMapper objectMapper = new ObjectMapper();

    private final CodeScanParser codeScanParser;
    private final DependabotParser dependabotParser;
    private final SecretScanParser secretScanParser;

    public ParserService(CodeScanParser codeScanParser,
                         DependabotParser dependabotParser,
                         SecretScanParser secretScanParser) {
        this.codeScanParser = codeScanParser;
        this.dependabotParser = dependabotParser;
        this.secretScanParser = secretScanParser;
    }

    public List<Finding> parse(ToolType toolType, String rawJson) {
        switch (toolType) {
            case CODESCAN:
                return codeScanParser.parse(rawJson);
            case DEPENDABOT:
                return dependabotParser.parse(rawJson);
            case SECRETSCAN:
                return secretScanParser.parse(rawJson);
            default:
                return Collections.emptyList();
        }
    }
}
