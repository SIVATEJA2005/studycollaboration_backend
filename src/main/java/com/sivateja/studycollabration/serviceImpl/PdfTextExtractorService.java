package com.sivateja.studycollabration.serviceImpl;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PdfTextExtractorService {

    // OLD method — kept for backwards compatibility
    public String extractText(String filePath) throws IOException {
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}", filePath, e);
            throw e;
        }
    }

    // ✅ NEW method — extract text from bytes (used with Cloudinary URL)
    public String extractTextFromBytes(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            log.error("Failed to extract text from PDF bytes", e);
            throw e;
        }
    }

    // Split text into chunks of ~500 words each (for embedding)
    public List<String> splitIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");

        StringBuilder chunk = new StringBuilder();
        int wordCount = 0;

        for (String word : words) {
            chunk.append(word).append(" ");
            wordCount++;

            if (wordCount >= chunkSize) {
                chunks.add(chunk.toString().trim());
                chunk = new StringBuilder();
                wordCount = 0;
            }
        }

        if (!chunk.isEmpty()) {
            chunks.add(chunk.toString().trim());
        }

        return chunks;
    }
}