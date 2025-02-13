package com.parser.Parser.Application.service.toolParser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parser.Parser.Application.model.Finding;
import com.parser.Parser.Application.model.ToolType;
import com.parser.Parser.utils.ParserUtils;

@Service
public class CodeScanParser {
    
    private final ObjectMapper objectMapper = ParserUtils.getObjectMapper();

    public List<Finding> parse(String rawJson) {
        List<Finding> findings = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(rawJson);

            if (root.isArray()) {
                for (JsonNode alert : root) {
                    findings.add(buildFindingFromCodeScan(alert));
                }
            } else {
                findings.add(buildFindingFromCodeScan(root));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return findings;
    }

    private Finding buildFindingFromCodeScan(JsonNode node) {
        Finding f = new Finding();
        f.setToolType(ToolType.CODESCAN);

        f.setId(UUID.randomUUID().toString());

        JsonNode rule = node.path("rule");
        f.setTitle(rule.path("name").asText("Unnamed CodeScan Alert"));

        f.setDescription(rule.path("full_description").asText(""));

        String rawState = node.path("state").asText("open").toLowerCase();
        String rawDismissedReason = node.path("dismissed_reason").asText("");
        f.setStatus(ParserUtils.mapStatus(rawState,rawDismissedReason, ToolType.CODESCAN));

        String rawSev = rule.path("security_severity_level").asText("medium");
        f.setSeverity(ParserUtils.mapSeverity(rawSev));

        f.setCreatedAt(LocalDateTime.now().toString());
        f.setUpdatedAt(LocalDateTime.now().toString());

        String cve = node.path("cve").asText("");
        f.setCve(cve);

        double cvssScore = node.path("cvss").asDouble(0.0);
        f.setCvss(cvssScore);

        f.setUrl(node.path("html_url").asText(""));

        List<String> cweList = new ArrayList<>();
        JsonNode tags = rule.path("tags");
        if (tags.isArray()) {
            for (JsonNode t : tags) {
                if (t.asText().contains("cwe/")) {
                    cweList.add(t.asText());
                }
            }
        }
        if (!cweList.isEmpty()) {
            f.setCwe(String.join(",", cweList));
        }

        JsonNode loc = node.path("most_recent_instance").path("location");
        if (!loc.isMissingNode()) {
            String path = loc.path("path").asText("");
            int startLine = loc.path("start_line").asInt(-1);
            f.setLocation(path + " (line " + startLine + ")");
        }

        Map<String, Object> leftover = objectMapper.convertValue(node, Map.class);

        f.setAdditionalData(leftover);

        return f;
    }
}
