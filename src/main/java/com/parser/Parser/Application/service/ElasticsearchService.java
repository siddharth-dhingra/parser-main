package com.parser.Parser.Application.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;

import com.parser.Parser.Application.model.Finding;
import com.parser.Parser.Application.model.ToolType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ElasticsearchService {

    private final ElasticsearchClient esClient;

    @Value("${app.elasticsearch.index}")
    private String indexName;

    public ElasticsearchService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    public List<Finding> findByToolType(ToolType toolType) throws IOException {
        SearchResponse<Finding> response = esClient.search(s -> s
                .index(indexName)
                .query(q -> q.term(t -> t
                        .field("toolType.keyword")
                        .value(toolType.name())  // match the enum name exactly
                ))
                .size(10_000), // just an upper bound
            Finding.class
        );

        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }

    public IndexResponse indexFinding(Finding finding) throws IOException {
        if (finding.getId() == null) {
            finding.setId(UUID.randomUUID().toString());
        }

        IndexRequest<Finding> request = IndexRequest.of(i -> i
                .index(indexName)
                .id(finding.getToolType()+finding.getId())
                .document(finding)
        );

        IndexResponse response = esClient.index(request);
        return response;
    }
}