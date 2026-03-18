package com.sivateja.studycollabration.dto.message;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MessageSendRequestDTO {

    private String content;
}