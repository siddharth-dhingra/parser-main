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
import com.parser.Parser.Application.model.Status;
import com.parser.Parser.Application.model.ToolType;
import com.parser.Parser.utils.ParserUtils;

@Service
public class DependabotParser {

    private final ObjectMapper objectMapper = ParserUtils.getObjectMapper();

    public List<Finding> parse(String rawJson) {
        List<Finding> findings = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(rawJson);

            if (root.isArray()) {
                for (JsonNode alert : root) {
                    findings.add(buildFindingFromDependabot(alert));
                }
            } else {
                findings.add(buildFindingFromDependabot(root));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return findings;
    }

    private Finding buildFindingFromDependabot(JsonNode node) {
        Finding f = new Finding();
        f.setToolType(ToolType.DEPENDABOT);

        // ID from "number"
        f.setId(UUID.randomUUID().toString());

        // Title from "security_advisory.summary"
        JsonNode advisory = node.path("security_advisory");
        f.setTitle(advisory.path("summary").asText("Unnamed Dependabot Alert"));
        f.setDescription(advisory.path("description").asText(""));

        f.setStatus(ParserUtils.mapStatus(node.path("state").asText("open")));

        // severity from "security_advisory.severity"
        String rawSev = advisory.path("severity").asText("medium");
        f.setSeverity(ParserUtils.mapSeverity(rawSev));

        f.setCreatedAt(LocalDateTime.now().toString());
        f.setUpdatedAt(LocalDateTime.now().toString());

        // url => "html_url"
        f.setUrl(node.path("html_url").asText(""));

        // cve => "security_advisory.cve_id"
        f.setCve(advisory.path("cve_id").asText(""));

        // cwe => from "cwes"[0].cwe_id if present
        JsonNode cwes = advisory.path("cwes");
        if (cwes.isArray() && cwes.size() > 0) {
            JsonNode first = cwes.get(0);
            f.setCwe(first.path("cwe_id").asText(""));
        }

        // cvss => from "security_advisory.cvss.score"
        double cvssScore = advisory.path("cvss").path("score").asDouble(0.0);
        f.setCvss(cvssScore);

        // location => "dependency.manifest_path"
        String manifestPath = node.path("dependency").path("manifest_path").asText("");
        f.setLocation(manifestPath);

        // leftover
//         Map<String, Object> leftover = new HashMap<>();
// //        leftover.put("dismissed_reason", node.path("dismissed_reason").asText(""));
// //        leftover.put("security_vulnerability", node.path("security_vulnerability").toString());
//         // Add the entire original JSON node as a string for reference
//         leftover.put("complete_data", node.toString());

        Map<String, Object> leftover = objectMapper.convertValue(node, Map.class);

        f.setAdditionalData(leftover);

        return f;
    }    
}
