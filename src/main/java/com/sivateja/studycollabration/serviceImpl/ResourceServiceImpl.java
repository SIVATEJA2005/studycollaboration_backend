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
//import com.sivateja.studycollabration.services.LinkPreviewService;
import com.sivateja.studycollabration.services.ResourceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final LinkPreviewServiceImpl linkPreviewService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public ResourceResponseDTO addLink(Long roomId, ResourceRequestDTO req, Users user) {
        Room room = getRoom(roomId);
//        checkMember(room, user);

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
        Path uploadPath = Paths.get(uploadDir, "resources", String.valueOf(roomId));
        Files.createDirectories(uploadPath);
        String originalName = file.getOriginalFilename();
        String storedName   = UUID.randomUUID() + "_" + originalName;
        Path filePath = uploadPath.resolve(storedName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        ResourceType type = detectType(file.getContentType());
        String fileUrl = "/api/resources/download/" + roomId + "/" + storedName;
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

        // delete physical file if uploaded
        if (resource.getType() != ResourceType.LINK) {
            try {
                String filename = Paths.get(resource.getUrl()).getFileName().toString();
                Path filePath = Paths.get(uploadDir, "resources",
                        String.valueOf(roomId), filename);
                Files.deleteIfExists(filePath);
            } catch (IOException ignored) {}
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

    private Room getRoom(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

//    private void checkMember(Room room, Users user) {
//        boolean isMember = room.getMembers().stream()
//                .filter(m -> m != null)
//                .anyMatch(m -> m.getId().equals(user.getId()));
//        if (!isMember)
//            throw new RuntimeException("Only room members can manage resources");
//    }

    private ResourceType detectType(String mimeType) {
        if (mimeType == null)                           return ResourceType.FILE;
        if (mimeType.equals("application/pdf"))         return ResourceType.PDF;
        if (mimeType.startsWith("image/"))              return ResourceType.IMAGE;
        if (mimeType.startsWith("video/"))              return ResourceType.VIDEO;
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