package org.vced.botmicroservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.vced.botmicroservice.config.BotConfigService;
import org.vced.ragutillib.authentication.CryptoUtils;
import org.vced.ragutillib.orm.AuthenticationDataORM;
import org.vced.ragutillib.orm.MessageDTO;
import org.vced.ragutillib.vector.VectorConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.Duration;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@PropertySource("classpath:application.yml")
@RequestMapping("/api/bot")
@Import(value = {VectorConfig.class})
public class BotController {
    private final ChatClient chatClient;
    private final CustomQuestionAnswerAdvisor questionAnswerAdvisor;
    private final MessageCustomChatMemory chatMemory;
    private final ObjectMapper mapper;
    private final BotConfigService botConfigService;
    private final ApplicationContext applicationContext;
    private final MeterRegistry meterRegistry;

    /**
     * Метод для генерации ответа, с учетом запроса пользователя. Генерация ответа происходи асинхронно с помощью Flux<>
     *
     * @param responseORM - текст полученного запроса
     *                    Декодируем запрос в нужную кодировку
     *                    Берем файл чанк с очереди, когда он появляется
     * @return поток ответа в виде Flux<String>
     */
    @GetMapping("/response/get")
    Flux<String> generation(
            @RequestParam(name = "request") String responseORM,
            @RequestParam(name = "x-user") String XUser) throws Exception {
        if (responseORM.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request cannot be empty");
        }
        String decodedRequest = URLDecoder.decode(responseORM, StandardCharsets.UTF_8);
        String decryptedAuth = CryptoUtils.decrypt(XUser);
        AuthenticationDataORM authenticationDataORM = mapper.readValue(decryptedAuth, AuthenticationDataORM.class);

        List<String> authorities = authenticationDataORM.getAuthorities() != null ? new ArrayList<>(authenticationDataORM.getAuthorities()) : List.of();

        var messageChatMemoryAdvisor = (MessageChatMemoryAdvisor) applicationContext
                .getBean(
                        "MessageChatMemoryAdvisor",
                        chatMemory,
                        authenticationDataORM.getName());

        return Flux.defer(() -> {
            Timer.Sample generationTimer = Timer.start(meterRegistry);
            return chatClient.prompt()
                .user(decodedRequest)
                .system(botConfigService.getSystemPrompt())
                .options(botConfigService.getOllamaOptions())
                .advisors(spec -> spec.params(Map.of(
                                        "query", decodedRequest,
                                        "x-user", XUser,
                                        "username", authenticationDataORM.getName(),
                                        "authorities", authorities
                                ))
                                .advisors(
                                        questionAnswerAdvisor,
                                        messageChatMemoryAdvisor
                                )
                )
                .stream()
                .content()
                .take(Duration.ofSeconds(100))
                .doOnError(error -> meterRegistry.counter("rag.llm.generation.errors").increment())
                .onErrorResume(WebClientResponseException.class, _ -> Flux.just("ОШИБКА ГЕНЕРАЦИИ ОТВЕТА: " +
                        "Сервер перегружен, недостаточно ресурсов (RAM/VRAM), повторите запрос позже"))
                .onErrorResume(WebClientRequestException.class, _ -> Flux.just("ОШИБКА ГЕНЕРАЦИИ ОТВЕТА: " +
                        "На сервере не запущена модель ИИ, сообщите администратору и повторите запрос позже"))
                .map(obj -> "'" + obj + "'")
                .concatWith(Flux.defer(questionAnswerAdvisor::getRetrievedImages))
                .concatWith(Mono.just("END"))
                .takeUntilOther(Flux.never())
                .doFinally(signal -> generationTimer.stop(meterRegistry.timer("rag.llm.generation")));
        });
    }

    @GetMapping("/dialogue/get")
    List<MessageDTO> messages(
            @RequestParam(name = "x-user") String XUser,
            @RequestParam(required = false) Integer limit
    ) throws Exception {
        if (XUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User should be logged in");
        }
        String decryptedAuth = CryptoUtils.decrypt(XUser);
        AuthenticationDataORM authenticationDataORM = mapper.readValue(decryptedAuth, AuthenticationDataORM.class);
        int effectiveLimit = (limit == null || limit <= 0 || limit > 400) ? 400 : limit;
        int dialogueSize = chatMemory.findByConversationId(authenticationDataORM.getName()).size();
        return chatMemory.findByConversationId(authenticationDataORM.getName())
                .stream()
                .map(message -> MessageDTO
                        .builder()
                        .textContent(message.getText())
                        .messageType(message.getMessageType())
                        .build())
                .skip(Math.max(dialogueSize - effectiveLimit, 0))
                .collect(Collectors.toList());
    }

    @GetMapping("/dialogue/clear")
    ResponseEntity<String> clear(@RequestParam(name = "x-user") String XUser) {
        if (XUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User should be logged in");
        }
        try {
            String decryptedAuth = CryptoUtils.decrypt(XUser);
            AuthenticationDataORM authenticationDataORM = mapper.readValue(decryptedAuth, AuthenticationDataORM.class);
            chatMemory.clear(authenticationDataORM.getName());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
