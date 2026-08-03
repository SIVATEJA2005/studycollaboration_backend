package com.sivateja.studycollabration.serviceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class EmbeddingService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String EMBEDDING_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent";

    public List<Double> getEmbedding(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                    "content", Map.of(
                            "parts", List.of(
                                    Map.of("text", text)
                            )
                    )
            );
            String url = EMBEDDING_URL + "?key=" + geminiApiKey;
            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Gemini API failed: " + response.getBody());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode valuesNode = root
                    .path("embedding")
                    .path("values");
            List<Double> embedding = new ArrayList<>();
            for (JsonNode val : valuesNode) {
                embedding.add(val.asDouble());
            }
            log.info("✅ Gemini embedding generated, size: {}", embedding.size());
            return embedding;

        } catch (Exception e) {
            log.error("❌ Failed to generate embedding", e);
            throw new RuntimeException("Embedding failed: " + e.getMessage(), e);
        }
    }
}
