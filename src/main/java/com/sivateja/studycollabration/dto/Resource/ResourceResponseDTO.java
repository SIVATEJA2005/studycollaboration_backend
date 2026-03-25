package com.sivateja.studycollabration.dto.Resource;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ResourceResponseDTO {
    private Long id;
    private String title;
    private String url;
    private String type;
    private String originalFileName;
    private Long fileSize;
    private String previewImage;
    private String previewDesc;
    private String siteName;
    private Long roomId;
    private Long addedById;
    private String addedBy;
    private LocalDateTime createdAt;
}