package com.parser.Parser.Application.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;

import com.parser.Parser.Application.model.Finding;
import com.parser.Parser.Application.model.ToolType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ElasticsearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchService.class);

    private final ElasticsearchClient esClient;

    public ElasticsearchService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    public List<Finding> findByToolType(String esIndex, ToolType toolType) throws IOException {

        ensureIndexExists(esIndex);
        
        SearchResponse<Finding> response = esClient.search(s -> s
                .index(esIndex)
                .query(q -> q.term(t -> t
                        .field("toolType.keyword")
                        .value(toolType.name()) 
                ))
                .size(10_000), 
            Finding.class
        );

        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }

    public IndexResponse indexFinding(String esIndex, Finding finding) throws IOException {

        ensureIndexExists(esIndex);

        if (finding.getId() == null) {
            finding.setId(UUID.randomUUID().toString());
        }

        IndexRequest<Finding> request = IndexRequest.of(i -> i
                .index(esIndex)
                .id(finding.getToolType()+finding.getId())
                .document(finding)
        );

        IndexResponse response = esClient.index(request);
        return response;
    }

    private void ensureIndexExists(String index) throws IOException {
        try {
            // Check if the index exists.
            boolean exists = esClient.indices().exists(e -> e.index(index)).value();
            if (!exists) {
                LOGGER.info("Index {} does not exist. Creating index...", index);
                CreateIndexResponse createIndexResponse = esClient.indices().create(c -> c
                        .index(index)
                        .settings(s -> s
                                .numberOfShards("1")
                                .numberOfReplicas("1")
                        )
                );
                if (!createIndexResponse.acknowledged()) {
                    String errMsg = "Failed to create index: " + index;
                    LOGGER.error(errMsg);
                    throw new RuntimeException(errMsg);
                }
                LOGGER.info("Index {} created successfully.", index);
            } else {
                LOGGER.info("Index {} already exists.", index);
            }
        } catch (Exception e) {
            LOGGER.error("Error ensuring index {} exists.", index, e);
            throw e;
        }
    }
}