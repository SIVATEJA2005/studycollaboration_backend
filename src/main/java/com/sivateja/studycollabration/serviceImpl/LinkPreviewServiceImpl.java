package com.sivateja.studycollabration.serviceImpl;

//package com.sivateja.studycollabration.services;

//import com.sivateja.studycollabration.dto.resource.LinkPreviewDTO;
import com.sivateja.studycollabration.dto.Resource.LinkPreviewDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class LinkPreviewServiceImpl {

    public LinkPreviewDTO getPreview(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(5000)
                    .get();

            String title       = doc.select("meta[property=og:title]").attr("content");
            String description = doc.select("meta[property=og:description]").attr("content");
            String image       = doc.select("meta[property=og:image]").attr("content");
            String siteName    = doc.select("meta[property=og:site_name]").attr("content");

            if (title.isBlank()) title = doc.title();

            return LinkPreviewDTO.builder()
                    .url(url)
                    .title(title)
                    .description(description.isBlank() ? null : description)
                    .image(image.isBlank() ? null : image)
                    .siteName(siteName.isBlank() ? null : siteName)
                    .build();

        } catch (Exception e) {
            return LinkPreviewDTO.builder()
                    .url(url)
                    .title(url)
                    .build();
        }
    }
}