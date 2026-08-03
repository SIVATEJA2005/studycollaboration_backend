package com.sivateja.studycollabration.serviceImpl;

import com.sivateja.studycollabration.dto.Resource.LinkPreviewDTO;
import com.sivateja.studycollabration.dto.Resource.ResourceRequestDTO;
import com.sivateja.studycollabration.dto.Resource.ResourceResponseDTO;
import com.sivateja.studycollabration.entities.Resource;
import com.sivateja.studycollabration.entities.Room;
import com.sivateja.studycollabration.entities.Users;
import com.sivateja.studycollabration.model.ResourceType;
import com.sivateja.studycollabration.repository.ResourceRepository;
import com.sivateja.studycollabration.repository.RoomRepository;
import com.sivateja.studycollabration.services.ResourceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final LinkPreviewServiceImpl linkPreviewService;
    private final CloudinaryService cloudinaryService;
    private final PdfTextExtractorService pdfTextExtractorService;
    private final EmbeddingService embeddingService;
    private final PineconeService pineconeService;

    @Override
    public ResourceResponseDTO addLink(Long roomId, ResourceRequestDTO req, Users user) {
        Room room = getRoom(roomId);
        Resource resource = Resource.builder()
                .title(req.getTitle() != null && !req.getTitle().isBlank()
                        ? req.getTitle() : req.getUrl())
                .url(req.getUrl())
                .type(ResourceType.LINK)
                .previewImage(req.getPreviewImage())
                .previewDesc(req.getPreviewDesc())
                .siteName(req.getSiteName())
                .room(room)
                .addedBy(user)
                .build();

        resource = resourceRepository.save(resource);
        ResourceResponseDTO response = toResponse(resource);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/resources", response);
        return response;
    }

    @Override
    @Transactional
    public ResourceResponseDTO uploadFile(Long roomId, MultipartFile file,
                                          String title, Users user) throws IOException {
        Room room = getRoom(roomId);

        String fileUrl = cloudinaryService.uploadFile(file, "resources/" + roomId);

        ResourceType type = detectType(file.getContentType());
        String originalName = file.getOriginalFilename();

        Resource resource = Resource.builder()
                .title(title != null && !title.isBlank() ? title : originalName)
                .url(fileUrl)
                .type(type)
                .originalFileName(originalName)
                .fileSize(file.getSize())
                .room(room)
                .addedBy(user)
                .build();

        resource = resourceRepository.save(resource);

        if (type == ResourceType.PDF) {
            indexPdfAsync(
                    fileUrl,
                    originalName,
                    String.valueOf(resource.getId()),
                    String.valueOf(roomId)
            );
        }

        ResourceResponseDTO response = toResponse(resource);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/resources", response);
        return response;
    }

    @Override
    public List<ResourceResponseDTO> getResources(Long roomId) {
        return resourceRepository.findByRoomIdOrderByCreatedAtDesc(roomId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteResource(Long resourceId, Users user) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!resource.getAddedBy().getId().equals(user.getId()))
            throw new RuntimeException("Only the uploader can delete this resource");

        Long roomId = resource.getRoom().getId();

        // ✅ Delete from Cloudinary instead of local disk
        if (resource.getType() != ResourceType.LINK) {
            String publicId = cloudinaryService.extractPublicId(resource.getUrl());
            if (publicId != null) {
                cloudinaryService.deleteFile(publicId);
            }
        }

        // Delete vectors from Pinecone when PDF is deleted
        if (resource.getType() == ResourceType.PDF) {
            pineconeService.deleteVectorsByResourceId(String.valueOf(resourceId));
        }

        resourceRepository.delete(resource);
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/resources/delete", resourceId);
    }

    @Override
    public LinkPreviewDTO getPreview(String url) {
        return linkPreviewService.getPreview(url);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    // ✅ Updated to download PDF from Cloudinary URL for indexing
    private void indexPdfAsync(String fileUrl, String originalName,
                               String resourceId, String roomId) {
        new Thread(() -> {
            try {
                log.info("Starting PDF indexing for resource: {}", resourceId);

                byte[] pdfBytes = new java.net.URL(fileUrl).openStream().readAllBytes();

                String text = pdfTextExtractorService.extractTextFromBytes(pdfBytes);

                List<String> chunks = pdfTextExtractorService.splitIntoChunks(text, 500);

                for (int i = 0; i < chunks.size(); i++) {
                    String chunkText = chunks.get(i);
                    List<Double> embedding = embeddingService.getEmbedding(chunkText);

                    String vectorId = resourceId + "_chunk_" + i;
                    Map<String, String> metadata = Map.of(
                            "text", chunkText,
                            "fileName", originalName,
                            "roomId", roomId,
                            "resourceId", resourceId,
                            "chunkIndex", String.valueOf(i)
                    );
                    pineconeService.upsertVector(vectorId, embedding, metadata);
                }

                log.info("PDF indexed successfully: {} chunks for resource {}",
                        chunks.size(), resourceId);

            } catch (Exception e) {
                log.error("Failed to index PDF for resource: {}", resourceId, e);
            }
        }).start();
    }

    private Room getRoom(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    private ResourceType detectType(String mimeType) {
        if (mimeType == null)                       return ResourceType.FILE;
        if (mimeType.equals("application/pdf"))     return ResourceType.PDF;
        if (mimeType.startsWith("image/"))          return ResourceType.IMAGE;
        if (mimeType.startsWith("video/"))          return ResourceType.VIDEO;
        return ResourceType.FILE;
    }

    private ResourceResponseDTO toResponse(Resource r) {
        return ResourceResponseDTO.builder()
                .id(r.getId())
                .title(r.getTitle())
                .url(r.getUrl())
                .type(r.getType().name())
                .originalFileName(r.getOriginalFileName())
                .fileSize(r.getFileSize())
                .previewImage(r.getPreviewImage())
                .previewDesc(r.getPreviewDesc())
                .siteName(r.getSiteName())
                .roomId(r.getRoom().getId())
                .addedById(r.getAddedBy().getId())
                .addedBy(r.getAddedBy().getUserName())
                .createdAt(r.getCreatedAt())
                .build();
    }
}