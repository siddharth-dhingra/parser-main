package com.parser.Parser.Application.service.toolParser;

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

        // ID from "number"
        f.setId(UUID.randomUUID().toString());

        // f.setId(UUID.randomUUID().toString());

        // Title from "rule.name" or fallback
        JsonNode rule = node.path("rule");
        f.setTitle(rule.path("name").asText("Unnamed CodeScan Alert"));

        // Description from "rule.description"
        f.setDescription(rule.path("full_description").asText(""));

        // Status from "state"
        String rawState = node.path("state").asText("open");
        f.setStatus(ParserUtils.mapStatus(rawState));

        // Severity from "rule.security_severity_level"
        String rawSev = rule.path("security_severity_level").asText("medium");
        f.setSeverity(ParserUtils.mapSeverity(rawSev));

        // Handle optional CVE if present
        // Adjust the path to wherever CVE might actually appear in your data
        String cve = node.path("cve").asText("");
        f.setCve(cve);

        // Handle optional CVSS score if present
        // Adjust the path to wherever CVSS might actually appear in your data
        double cvssScore = node.path("cvss").asDouble(0.0);
        f.setCvss(cvssScore);

        // url = "html_url"
        f.setUrl(node.path("html_url").asText(""));

        // parse cwe from "rule.tags" if it contains something like "external/cwe/cwe-073"
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

        // location => node.most_recent_instance.location.path + line
        JsonNode loc = node.path("most_recent_instance").path("location");
        if (!loc.isMissingNode()) {
            String path = loc.path("path").asText("");
            int startLine = loc.path("start_line").asInt(-1);
            f.setLocation(path + " (line " + startLine + ")");
        }

        // leftover in additionalData:
        Map<String, Object> leftover = objectMapper.convertValue(node, Map.class);
//        leftover.put("toolName", node.path("tool").path("name").asText(""));
//        leftover.put("dismissed_reason", node.path("dismissed_reason").asText(""));
//        leftover.put("fixed_at", node.path("fixed_at").asText(""));

        // Add the entire original JSON node as a string for reference
//        leftover.put("complete_data", node.toString());

        f.setAdditionalData(leftover);

        return f;
    }
}
