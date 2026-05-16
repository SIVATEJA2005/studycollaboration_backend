package com.sivateja.studycollabration.serviceImpl;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AiChatService {

    private final EmbeddingService embeddingService;
    private final PineconeService pineconeService;

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Groq API endpoint
    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";
    // Best free model on Groq
    private static final String GROQ_MODEL = "llama-3.3-70b-versatile";

    // ── RAG: Answer question from room's study material ───────────────────────
    public String askQuestion(String question, String roomId) {
        // 1. Convert question to embedding
        List<Double> questionEmbedding = embeddingService.getEmbedding(question);

        // 2. Search Pinecone for relevant chunks
        List<Map<String, Object>> similarChunks =
                pineconeService.querySimilar(questionEmbedding, 5, roomId);

        // 3. Build context from retrieved chunks
        StringBuilder context = new StringBuilder();
        if (similarChunks.isEmpty()) {
            context.append("No study material found for this room yet.");
        } else {
            for (Map<String, Object> chunk : similarChunks) {
                context.append("From: ").append(chunk.get("fileName")).append("\n");
                context.append(chunk.get("text")).append("\n\n");
            }
        }

        // 4. Build prompt and call Groq
        String systemPrompt = """
                You are a helpful study assistant. Answer the student's question 
                based ONLY on the provided study material context below.
                If the answer is not in the context, clearly say so.
                Be concise and educational in your response.
                """;

        String userPrompt = "Study Material Context:\n" + context +
                "\n\nStudent Question: " + question;

        return callGroq(systemPrompt, userPrompt);
    }

    // ── Summarize a document ──────────────────────────────────────────────────
    public String summarize(String text) {
        String truncated = text.length() > 8000 ? text.substring(0, 8000) : text;
        String systemPrompt = """
                You are a study assistant. Summarize the following study material 
                in a clear, concise way. Use bullet points where appropriate.
                Focus on key concepts, definitions, and important points.
                """;
        return callGroq(systemPrompt, "Summarize this:\n\n" + truncated);
    }

    // ── Generate Quiz ─────────────────────────────────────────────────────────
    public String generateQuiz(String text, int numQuestions) {
        String truncated = text.length() > 8000 ? text.substring(0, 8000) : text;
        String systemPrompt = """
                You are a study assistant. Generate %d multiple choice quiz questions 
                from the provided study material.
                Format each question exactly like this:
                
                Q1. [Question]
                A) [Option]
                B) [Option]
                C) [Option]
                D) [Option]
                Answer: [Correct letter]
                
                Make questions educational and test understanding, not just memorization.
                """.formatted(numQuestions);
        return callGroq(systemPrompt, "Generate quiz from this:\n\n" + truncated);
    }

    // ── Helper: Call Groq API ─────────────────────────────────────────────────
    private String callGroq(String systemPrompt, String userPrompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            );

            Map<String, Object> body = new HashMap<>();
            body.put("model", GROQ_MODEL);
            body.put("messages", messages);
            body.put("max_tokens", 1000);
            body.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    GROQ_URL, request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0)
                    .path("message").path("content").asText();

        } catch (Exception e) {
            log.error("Groq call failed", e);
            throw new RuntimeException("AI response failed: " + e.getMessage());
        }
    }
}
