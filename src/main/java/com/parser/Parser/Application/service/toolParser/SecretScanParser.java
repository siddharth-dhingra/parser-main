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
import com.parser.Parser.Application.model.Severity;
import com.parser.Parser.Application.model.ToolType;
import com.parser.Parser.utils.ParserUtils;

@Service
public class SecretScanParser {

    private final ObjectMapper objectMapper = ParserUtils.getObjectMapper();

    public List<Finding> parse(String rawJson) {
        List<Finding> findings = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(rawJson);

            if (root.isArray()) {
                for (JsonNode alert : root) {
                    findings.add(buildFindingFromSecretScan(alert));
                }
            } else {
                findings.add(buildFindingFromSecretScan(root));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return findings;
    }

    private Finding buildFindingFromSecretScan(JsonNode node) {
        Finding f = new Finding();
        f.setToolType(ToolType.SECRETSCAN);

        f.setId(UUID.randomUUID().toString());

        f.setTitle(node.path("secret_type_display_name").asText("Secret Alert"));

        f.setDescription("Exposed secret of type: " + node.path("secret_type").asText(""));

        String rawState = node.path("state").asText("open");
        String resolution = node.path("resolution").asText("");
        f.setStatus(ParserUtils.mapStatus(rawState, resolution, ToolType.SECRETSCAN));

        f.setSeverity(Severity.CRITICAL);

        f.setCreatedAt(LocalDateTime.now().toString());
        f.setUpdatedAt(LocalDateTime.now().toString());

        f.setUrl(node.path("html_url").asText(""));

        f.setCve("");
        f.setCwe("");

        f.setCvss(0.0);

        f.setLocation("");

        f.setTicketId(null);

        Map<String, Object> leftover = objectMapper.convertValue(node, Map.class);

        f.setAdditionalData(leftover);

        return f;
    }
}
