package org.vced.botmicroservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vced.botmicroservice.CustomQuestionAnswerAdvisor;
import org.vced.botmicroservice.MessageCustomChatMemory;
import org.vced.ragutillib.authentication.CryptoUtils;
import org.vced.ragutillib.authority.ImportantAuthorities;
import org.vced.ragutillib.orm.AuthenticationDataORM;

import java.nio.file.AccessDeniedException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@PropertySource("classpath:application.yml")
@RequestMapping("/api/bot")
public class BotConfigController {
    private final ObjectMapper mapper;
    private final OllamaApi ollamaApi;
    private final BotConfigService botConfigService;
    private final MessageCustomChatMemory chatMemory;
    private final CustomQuestionAnswerAdvisor questionAnswerAdvisor;

    @PostMapping("/ollama/{path}")
    Object resolveOllamaApi(
            @PathVariable String path,
            @RequestParam(name = "x-user") String XUser,
            @RequestBody(required = false) Map<String, String> model
    ) throws Exception {
        if (!isAdmin(XUser)) {
            throw new AccessDeniedException("Access denied");
        }
        return switch (path) {
            case "tags" -> ollamaApi.listModels();
            case "show" -> ollamaApi.showModel(new OllamaApi.ShowModelRequest(model.get("model")));
            default -> throw new IllegalStateException("Unexpected value: " + path);
        };
    }

    @GetMapping(value = "/config", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<BotConfigDTO> getBotConfigService(@RequestParam(name = "x-user") String XUser)
            throws AccessDeniedException {
        if (!isAdmin(XUser)) {
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(
                botConfigService.getConfig());
    }

    @PutMapping("/config")
    public ResponseEntity<Void> updateBotConfig(@RequestBody BotConfigDTO dto,
                                                @RequestParam(name = "x-user") String XUser)
            throws IllegalArgumentException, AccessDeniedException {
        if (!isAdmin(XUser)) {
            throw new AccessDeniedException("Access denied");
        }
        botConfigService.saveConfig(dto, questionAnswerAdvisor, chatMemory);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/config/reset")
    public ResponseEntity<Void> resetBotConfig(@RequestParam(name = "x-user") String XUser)
            throws IllegalArgumentException, AccessDeniedException {
        if (!isAdmin(XUser)) {
            throw new AccessDeniedException("Access denied");
        }
        botConfigService.resetConfig(questionAnswerAdvisor, chatMemory);
        return ResponseEntity.ok().build();
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleException(IllegalArgumentException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    private boolean isAdmin(String xUser) {
        try {
            String decryptedAuth = CryptoUtils.decrypt(xUser);
            AuthenticationDataORM authData = mapper.readValue(decryptedAuth, AuthenticationDataORM.class);
            return authData.getAuthorities().contains(ImportantAuthorities.ADMIN_ROLE.getId());
        } catch (Exception e) {
            return false;
        }
    }
}
