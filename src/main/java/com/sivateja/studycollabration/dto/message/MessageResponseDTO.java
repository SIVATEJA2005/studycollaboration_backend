package com.sivateja.studycollabration.dto.message;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MessageResponseDTO {

    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;
}
