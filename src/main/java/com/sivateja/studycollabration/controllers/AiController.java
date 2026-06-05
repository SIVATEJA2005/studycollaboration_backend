package com.sivateja.studycollabration.controllers;
import com.sivateja.studycollabration.Security.CustomUserDetails;
import com.sivateja.studycollabration.serviceImpl.AiChatService;
import com.sivateja.studycollabration.serviceImpl.EmbeddingService;
import com.sivateja.studycollabration.serviceImpl.PdfTextExtractorService;
import com.sivateja.studycollabration.serviceImpl.PineconeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiChatService aiChatService;
    private final PdfTextExtractorService pdfTextExtractorService;
    private final EmbeddingService embeddingService;
    private final PineconeService pineconeService;

    // ✅ REMOVED: uploadDir — no longer needed, files are on Cloudinary now

    // POST /api/ai/index-pdf
    // Now accepts cloudinaryUrl instead of local fileName
    @PostMapping("/index-pdf")
    public ResponseEntity<?> indexPdf(
            @RequestParam Long roomId,
            @RequestParam String cloudinaryUrl,  // ✅ changed from fileName
            @RequestParam String originalName,
            @RequestParam String resourceId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        try {
            // ✅ Download PDF bytes from Cloudinary URL
            byte[] pdfBytes = new java.net.URL(cloudinaryUrl).openStream().readAllBytes();

            // 1. Extract text from bytes
            String text = pdfTextExtractorService.extractTextFromBytes(pdfBytes);

            // 2. Split into chunks
            List<String> chunks = pdfTextExtractorService.splitIntoChunks(text, 500);

            // 3. Embed each chunk and store in Pinecone
            for (int i = 0; i < chunks.size(); i++) {
                String chunkText = chunks.get(i);
                List<Double> embedding = embeddingService.getEmbedding(chunkText);

                String vectorId = resourceId + "_chunk_" + i;
                Map<String, String> metadata = Map.of(
                        "text", chunkText,
                        "fileName", originalName,
                        "roomId", String.valueOf(roomId),
                        "resourceId", resourceId,
                        "chunkIndex", String.valueOf(i)
                );
                pineconeService.upsertVector(vectorId, embedding, metadata);
            }

            return ResponseEntity.ok(Map.of(
                    "message", "PDF indexed successfully",
                    "chunks", chunks.size()
            ));

        } catch (Exception e) {
            log.error("Failed to index PDF", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/ai/ask
    // RAG: ask a question about room's study material — NO CHANGE NEEDED ✅
    @PostMapping("/ask")
    public ResponseEntity<?> ask(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            String question = body.get("question");
            String roomId = body.get("roomId");

            if (question == null || question.isBlank())
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Question is required"));

            String answer = aiChatService.askQuestion(question, roomId);
            return ResponseEntity.ok(Map.of("answer", answer));

        } catch (Exception e) {
            log.error("AI ask failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/ai/summarize
    // ✅ Now accepts cloudinaryUrl instead of local fileName
    @PostMapping("/summarize")
    public ResponseEntity<?> summarize(
            @RequestParam String cloudinaryUrl,  // ✅ changed from roomId + fileName
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // ✅ Download PDF bytes from Cloudinary URL
            byte[] pdfBytes = new java.net.URL(cloudinaryUrl).openStream().readAllBytes();
            String text = pdfTextExtractorService.extractTextFromBytes(pdfBytes);
            String summary = aiChatService.summarize(text);
            return ResponseEntity.ok(Map.of("summary", summary));

        } catch (Exception e) {
            log.error("Summarize failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/ai/quiz
    // ✅ Now accepts cloudinaryUrl instead of local fileName
    @PostMapping("/quiz")
    public ResponseEntity<?> generateQuiz(
            @RequestParam String cloudinaryUrl,  // ✅ changed from roomId + fileName
            @RequestParam(defaultValue = "5") int numQuestions,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // ✅ Download PDF bytes from Cloudinary URL
            byte[] pdfBytes = new java.net.URL(cloudinaryUrl).openStream().readAllBytes();
            String text = pdfTextExtractorService.extractTextFromBytes(pdfBytes);
            String quiz = aiChatService.generateQuiz(text, numQuestions);
            return ResponseEntity.ok(Map.of("quiz", quiz));

        } catch (Exception e) {
            log.error("Quiz generation failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE /api/ai/index/{resourceId} — NO CHANGE NEEDED ✅
    @DeleteMapping("/index/{resourceId}")
    public ResponseEntity<?> deleteIndex(@PathVariable String resourceId) {
        try {
            pineconeService.deleteVectorsByResourceId(resourceId);
            return ResponseEntity.ok(Map.of("message", "Vectors deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}