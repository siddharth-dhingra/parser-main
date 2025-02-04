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
import com.parser.Parser.Application.model.Status;
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

        // id => "number"
        f.setId(UUID.randomUUID().toString());

        // title => "secret_type_display_name"
        f.setTitle(node.path("secret_type_display_name").asText("Secret Alert"));

        // description => "secret_type"
        f.setDescription("Exposed secret of type: " + node.path("secret_type").asText(""));

        f.setStatus(ParserUtils.mapStatus(node.path("state").asText("open")));

        // severity? fallback to MEDIUM unless you have some logic to adjust
        f.setSeverity(Severity.CRITICAL);

        f.setCreatedAt(LocalDateTime.now().toString());
        f.setUpdatedAt(LocalDateTime.now().toString());

        // url => "html_url"
        f.setUrl(node.path("html_url").asText(""));

        // cve, cwe => not typical in secrets
        f.setCve("");
        f.setCwe("");

        // no CVSS
        f.setCvss(0.0);

        // location => optional
        f.setLocation("");

        // leftover
//         Map<String, Object> leftover = new HashMap<>();
// //        leftover.put("secret", node.path("secret").asText(""));
// //        leftover.put("validity", node.path("validity").asText(""));
// //        leftover.put("publicly_leaked", node.path("publicly_leaked").asBoolean(false));
//         // Add the entire original JSON node as a string for reference
//         leftover.put("complete_data", node.toString());

        Map<String, Object> leftover = objectMapper.convertValue(node, Map.class);

        f.setAdditionalData(leftover);

        return f;
    }
}
