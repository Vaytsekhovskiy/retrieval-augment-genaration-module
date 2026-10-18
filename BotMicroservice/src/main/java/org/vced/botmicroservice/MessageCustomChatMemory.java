package org.vced.botmicroservice;

import io.micrometer.common.lang.NonNullApi;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NonNullApi
public class MessageCustomChatMemory implements ChatMemory {
    private static final int DEFAULT_MAX_MESSAGES = 20; // кол-во сообщений, которые посылаются в контекст нейростеи

    private final ChatMemoryRepository chatMemoryRepository;

    @Setter
    @Getter
    private Integer maxMessages;
    private static final int DEFAULT_MAX_SAVED_MESSAGES = 2000; // кол-во сообщений, которые сохраняются в базе данных

    private MessageCustomChatMemory(ChatMemoryRepository chatMemoryRepository, int maxMessages) {
        Assert.notNull(chatMemoryRepository, "chatMemoryRepository cannot be null");
        Assert.isTrue(maxMessages > 0, "maxMessages must be greater than 0");
        this.chatMemoryRepository = chatMemoryRepository;
        this.maxMessages = maxMessages;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");

        List<Message> memoryMessages = this.chatMemoryRepository.findByConversationId(conversationId);
        List<Message> processedMessages = process(memoryMessages, messages);
        this.chatMemoryRepository.saveAll(conversationId, processedMessages);
    }

    @Override
    public List<Message> get(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        List<Message> messages = this.chatMemoryRepository.findByConversationId(conversationId);
        if (messages.size() <= this.maxMessages) return messages;
        // Возвращаем последние maxMessages сообщений
        return messages.subList(messages.size() - this.maxMessages, messages.size());
    }

    @Override
    public void clear(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        this.chatMemoryRepository.deleteByConversationId(conversationId);
    }

    private List<Message> process(List<Message> memoryMessages, List<Message> newMessages) {
        List<Message> processedMessages = new ArrayList<>();

        Set<Message> memoryMessagesSet = new HashSet<>(memoryMessages);
        boolean hasNewSystemMessage = newMessages.stream()
                .filter(SystemMessage.class::isInstance)
                .anyMatch(message -> !memoryMessagesSet.contains(message));

        memoryMessages.stream()
                .filter(message -> !(hasNewSystemMessage && message instanceof SystemMessage))
                .forEach(processedMessages::add);

        processedMessages.addAll(newMessages);

        if (processedMessages.size() > DEFAULT_MAX_SAVED_MESSAGES) {
            processedMessages = processedMessages.stream()
                    .skip(processedMessages.size() - DEFAULT_MAX_SAVED_MESSAGES)
                    .collect(Collectors.toList());
        }

        return processedMessages;
    }

    public static MessageCustomChatMemory.Builder builder() {
        return new MessageCustomChatMemory.Builder();
    }

    public List<Message> findByConversationId(String name) {
        return chatMemoryRepository.findByConversationId(name);
    }

    public static final class Builder {

        private ChatMemoryRepository chatMemoryRepository;

        private int maxMessages = DEFAULT_MAX_MESSAGES;

        private Builder() {
        }

        public MessageCustomChatMemory.Builder chatMemoryRepository(ChatMemoryRepository chatMemoryRepository) {
            this.chatMemoryRepository = chatMemoryRepository;
            return this;
        }

        public MessageCustomChatMemory.Builder maxMessages(Integer maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        public MessageCustomChatMemory build() {
            if (this.chatMemoryRepository == null) {
                this.chatMemoryRepository = new InMemoryChatMemoryRepository();
            }
            return new MessageCustomChatMemory(this.chatMemoryRepository, this.maxMessages);
        }

    }
}
