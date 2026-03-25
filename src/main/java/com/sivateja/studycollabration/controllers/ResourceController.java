package com.sivateja.studycollabration.controllers;
import com.sivateja.studycollabration.dto.Resource.LinkPreviewDTO;
import com.sivateja.studycollabration.dto.Resource.ResourceRequestDTO;
import com.sivateja.studycollabration.dto.Resource.ResourceResponseDTO;
import com.sivateja.studycollabration.entities.Users;
import com.sivateja.studycollabration.repository.UserRepository;
import com.sivateja.studycollabration.services.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private Users getUser(UserDetails u) {
        return userRepository.findByUserName(u.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    @PostMapping("/link/{roomId}")
    public ResponseEntity<ResourceResponseDTO> addLink(
            @PathVariable Long roomId,
            @RequestBody ResourceRequestDTO req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                resourceService.addLink(roomId, req, getUser(userDetails)));
    }
    // POST /api/resources/upload/{roomId}
    @PostMapping("/upload/{roomId}")
    public ResponseEntity<ResourceResponseDTO> uploadFile(
            @PathVariable Long roomId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        return ResponseEntity.ok(
                resourceService.uploadFile(roomId, file, title, getUser(userDetails)));
    }
    // GET /api/resources/room/{roomId}
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ResourceResponseDTO>> getResources(
            @PathVariable Long roomId) {
        return ResponseEntity.ok(resourceService.getResources(roomId));
    }
    // GET /api/resources/preview?url=...
    @GetMapping("/preview")
    public ResponseEntity<LinkPreviewDTO> getPreview(@RequestParam String url) {
        return ResponseEntity.ok(resourceService.getPreview(url));
    }

    // GET /api/resources/download/{roomId}/{filename}
    @GetMapping("/download/{roomId}/{filename}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(
            @PathVariable Long roomId,
            @PathVariable String filename) throws MalformedURLException {
        Path filePath = Paths.get(uploadDir, "resources",
                String.valueOf(roomId), filename);
        org.springframework.core.io.Resource resource =
                new UrlResource(filePath.toUri());

        if (!resource.exists())
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    // DELETE /api/resources/{resourceId}
    @DeleteMapping("/{resourceId}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal UserDetails userDetails) {
        resourceService.deleteResource(resourceId, getUser(userDetails));
        return ResponseEntity.ok().build();
    }
}