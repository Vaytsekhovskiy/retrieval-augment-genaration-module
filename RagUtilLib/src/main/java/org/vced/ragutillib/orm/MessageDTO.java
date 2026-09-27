package org.vced.ragutillib.orm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.ai.chat.messages.MessageType;

@Setter
@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private String textContent;
    private MessageType messageType;
}
