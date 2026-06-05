package com.sivateja.studycollabration.serviceImpl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String resourceType = "raw"; // ✅ default for PDFs

        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
            resourceType = "image";
        } else if (contentType != null && contentType.startsWith("video/")) {
            resourceType = "video";
        }

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "studycollab/" + folder,
                        "resource_type", resourceType
                )
        );
        return (String) uploadResult.get("secure_url");
    }
    public void deleteFile(String publicId) {
        try {
            // ✅ Detect resource type from publicId/URL
            String resourceType = "raw"; // default for PDFs/files
            if (publicId.contains("/image/")) {
                resourceType = "image";
            } else if (publicId.contains("/video/")) {
                resourceType = "video";
            }

            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", resourceType  // ✅ no more "auto"
            ));
        } catch (IOException e) {
            log.error("Failed to delete file from Cloudinary: {}", publicId, e);
        }
    }

    // Extract public_id from a Cloudinary URL for deletion
    public String extractPublicId(String cloudinaryUrl) {
        // URL format: https://res.cloudinary.com/cloud-name/resource-type/upload/v123/folder/filename.ext
        try {
            String[] parts = cloudinaryUrl.split("/upload/");
            String afterUpload = parts[1]; // e.g. v1234567/studycollab/resources/filename.pdf
            // Remove version prefix (v1234567/)
            String withoutVersion = afterUpload.replaceFirst("v\\d+/", "");
            // Remove file extension
            int dotIndex = withoutVersion.lastIndexOf(".");
            return dotIndex != -1 ? withoutVersion.substring(0, dotIndex) : withoutVersion;
        } catch (Exception e) {
            log.error("Failed to extract public ID from URL: {}", cloudinaryUrl);
            return null;
        }
    }
}