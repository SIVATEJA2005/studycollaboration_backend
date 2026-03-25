package com.sivateja.studycollabration.dto.Resource;

//package com.sivateja.studycollabration.dto.resource;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LinkPreviewDTO {
    private String url;
    private String title;
    private String description;
    private String image;
    private String siteName;
}