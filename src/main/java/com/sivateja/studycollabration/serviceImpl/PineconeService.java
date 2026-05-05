package com.sivateja.studycollabration.serviceImpl;



import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Slf4j
@Service
public class PineconeService {

    @Value("${pinecone.api.key}")
    private String pineconeApiKey;

    @Value("${pinecone.host}")
    private String pineconeHost;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Upsert (store) a vector with metadata into Pinecone
    public void upsertVector(String id, List<Double> embedding,
                             Map<String, String> metadata) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Api-Key", pineconeApiKey);

            Map<String, Object> vector = new HashMap<>();
            vector.put("id", id);
            vector.put("values", embedding);
            vector.put("metadata", metadata);

            Map<String, Object> body = Map.of("vectors", List.of(vector));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(pineconeHost + "/vectors/upsert",
                    request, String.class);

            log.info("Upserted vector with id: {}", id);

        } catch (Exception e) {
            log.error("Failed to upsert vector to Pinecone", e);
            throw new RuntimeException("Pinecone upsert failed: " + e.getMessage());
        }
    }

    // Query Pinecone for top-k similar vectors
    public List<Map<String, Object>> querySimilar(List<Double> embedding,
                                                  int topK,
                                                  String roomId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Api-Key", pineconeApiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("vector", embedding);
            body.put("topK", topK);
            body.put("includeMetadata", true);
            // Filter by roomId so users only search their room's documents
            body.put("filter", Map.of("roomId", roomId));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    pineconeHost + "/query", request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode matches = root.path("matches");

            List<Map<String, Object>> results = new ArrayList<>();
            for (JsonNode match : matches) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", match.path("id").asText());
                result.put("score", match.path("score").asDouble());
                result.put("text", match.path("metadata").path("text").asText());
                result.put("fileName", match.path("metadata").path("fileName").asText());
                results.add(result);
            }
            return results;

        } catch (Exception e) {
            log.error("Failed to query Pinecone", e);
            throw new RuntimeException("Pinecone query failed: " + e.getMessage());
        }
    }

    // Delete all vectors for a specific resource (when file is deleted)
    public void deleteVectorsByResourceId(String resourceId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Api-Key", pineconeApiKey);

            Map<String, Object> body = Map.of(
                    "filter", Map.of("resourceId", resourceId)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(pineconeHost + "/vectors/delete",
                    request, String.class);

            log.info("Deleted vectors for resourceId: {}", resourceId);

        } catch (Exception e) {
            log.error("Failed to delete vectors from Pinecone", e);
        }
    }
}
