package org.vced.mainmicroservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.vced.ragutillib.document.FoldersInfoDTO;
import org.vced.ragutillib.orm.MessageDTO;
import org.vced.ragutillib.orm.SuggestedDocumentORM;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiHandler {
    private final WebClient webClient;
    ParameterizedTypeReference<List<SuggestedDocumentORM>> suggestedDocsORMTypeRef =
            new ParameterizedTypeReference<>() {};
    ParameterizedTypeReference<List<MessageDTO>> suggestedMessageDTOTypeRef =
            new ParameterizedTypeReference<>() {};

    public List<SuggestedDocumentORM> getSuggestedDocuments(String request, String XUser) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/search/files/get")
                            .queryParam("request", request)
                            .queryParam("x-user", XUser)
                            .build()
                    )
                    .retrieve()
                    .bodyToMono(suggestedDocsORMTypeRef)
                    .block();

        } catch (Exception e) {
            log.error(e.getMessage());
            return fallbackResult();
        }
    }

    private List<SuggestedDocumentORM> fallbackResult() {
        return List.of(
                SuggestedDocumentORM.builder()
                        .fileName("false")
                        .id(1)
                        .relevantContent("")
                        .build()
        );
    }

    public List<MessageDTO> getMessages(String XUser) {
        List<MessageDTO> messages = new ArrayList<>();
        try {
             messages = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/bot/dialogue/get")
                            .queryParam("x-user", XUser)
                            .build()
                    )
                    .retrieve()
                    .bodyToMono(suggestedMessageDTOTypeRef)
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch dialogue for {}: {}", XUser, e.getMessage());
        }
        return messages;
    }

    public ResponseEntity<?> clearDialogue(String XUser) {
        try {
            webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/bot/dialogue/clear")
                            .queryParam("x-user", XUser)
                            .build()
                    )
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка сервера");
        }
    }
    public FoldersInfoDTO getFolderInfo(String XUser) {
        try {
            FoldersInfoDTO foldersInfoDTO = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/convert/folders")
                            .queryParam("x-user", XUser)
                            .build()
                    )
                    .retrieve()
                    .bodyToMono(FoldersInfoDTO.class)
                    .block();
            return foldersInfoDTO;

        } catch (Exception e) {
            log.error("Failed to get folder info for user {}: {}", XUser, e.getMessage());
            return FoldersInfoDTO.builder()
                    .rootFoldersPaths(List.of())
                    .exceptFolderPaths(List.of())
                    .mountPoint("")
                    .build();
        }
    }
}
