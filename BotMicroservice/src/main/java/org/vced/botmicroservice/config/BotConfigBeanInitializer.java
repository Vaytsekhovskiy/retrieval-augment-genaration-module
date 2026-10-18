package org.vced.botmicroservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.vced.botmicroservice.CustomQuestionAnswerAdvisor;
import org.vced.botmicroservice.MessageCustomChatMemory;
import org.vced.ragutillib.document.ArticleService;
import org.vced.ragutillib.document.DocumentService;
import org.vced.ragutillib.vector.ChunkService;

/**
 * Класс конфигурации бота
 * @author Егор
 *  */
@Configuration
@EnableJpaRepositories(basePackages = {"org.vced.botmicroservice.config"})
@EntityScan(basePackages = {"org.vced.botmicroservice.config"})
@ComponentScan(basePackages = {"org.vced.botmicroservice.config"})
@Slf4j
@PropertySource("classpath:application.yml")
@RequiredArgsConstructor
public class BotConfigBeanInitializer {
    @Value("${microservice.gateway}")
    private String BASE_URL;
    @Value("${youtrack.api.token}")
    private String token;
    @Value("${spring.application.print-images}")
    private boolean isPrintImages;
    private final BotConfigService botConfigService;
    /**
     * @param builder - ласс бля создания api
     * @return API для работы с ai моделью, с учетом промпт
     * Создает и возвращает chatclient с учетом системного промпта
     * Если не получилось создать с системным промптом, возвращаем обычное AP*/
    @Bean
    ChatClient chatClient(ChatClient.Builder builder){
        return builder
                .defaultOptions(botConfigService.getOllamaOptions())
                .build();
    }
    @Bean(name = "MessageChatMemoryAdvisor")
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory, String conversationId) {
        return MessageChatMemoryAdvisor.builder(chatMemory)
                .conversationId(conversationId)
                .order(1)
                .build();
    }
    @Bean
    CustomQuestionAnswerAdvisor questionAnswerAdvisor(
            ChunkService chunkService,
            DocumentService documentService,
            ArticleService articleService
    ){
        return new CustomQuestionAnswerAdvisor(
                chunkService,
                documentService,
                articleService,
                BASE_URL,
                token,
                isPrintImages,
                botConfigService.getRagPrompt(),
                botConfigService,
                2
        );
    }
    @Bean
    MessageCustomChatMemory messageCustomChatMemory(JdbcChatMemoryRepository chatMemoryRepository){
        var chatMemoryMaxMessages = botConfigService.getChatMemoryMaxMessages();
        if (chatMemoryMaxMessages % 2 != 0) {
            log.error("spring.ai.chat.memory.messages can not be odd, " +
                    "change spring.ai.chat.memory.messages to spring.ai.chat.memory.messages + 1");
            chatMemoryMaxMessages++;
        }
        var messageCustomChatMemory = MessageCustomChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(chatMemoryMaxMessages)
                .build();
        log.info("MessageCustomChatMemory was successfully created with spring.ai.chat.memory.messages = {}",
                chatMemoryMaxMessages);
        return messageCustomChatMemory;
    }
}