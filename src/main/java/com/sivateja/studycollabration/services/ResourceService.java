package com.sivateja.studycollabration.services;
import com.sivateja.studycollabration.dto.Resource.LinkPreviewDTO;
import com.sivateja.studycollabration.dto.Resource.ResourceRequestDTO;
import com.sivateja.studycollabration.dto.Resource.ResourceResponseDTO;
import com.sivateja.studycollabration.entities.Users;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface ResourceService {
    ResourceResponseDTO addLink(Long roomId, ResourceRequestDTO req, Users user);
    ResourceResponseDTO uploadFile(Long roomId, MultipartFile file, String title, Users user) throws IOException;
    List<ResourceResponseDTO> getResources(Long roomId);
    void deleteResource(Long resourceId, Users user);
    LinkPreviewDTO getPreview(String url);
}