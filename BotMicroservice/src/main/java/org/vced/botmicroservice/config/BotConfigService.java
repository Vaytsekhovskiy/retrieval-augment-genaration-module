package org.vced.botmicroservice.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vced.botmicroservice.CustomQuestionAnswerAdvisor;
import org.vced.botmicroservice.MessageCustomChatMemory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotConfigService {
    @Value("${spring.ai.prompt}")
    private String systemPromptPath;
    @Value("${spring.ai.rag-prompt}")
    private String ragPromptPath;
    @Getter
    @Setter
    private String systemPrompt;
    @Getter
    private String ragPrompt;
    @Getter
    @Value("${spring.ai.chat.memory.messages}")
    private Integer chatMemoryMaxMessages;
    @Value("${spring.ai.request.topK}")
    @Getter
    private int topK;
    @Value("${spring.ai.request.similarity-threshold}")
    @Getter
    private double similarityThreshold;
    @Value("${spring.ai.ollama.chat.model}")
    @Getter
    private String chatModelName;
    @Value("${spring.ai.request.ignore-authorities}")
    @Getter
    @Setter
    private boolean ignoreAuthorities;

    // Параметры LLM - только аннотации @Value без @Getter @Setter
    @Value("${spring.ai.ollama.temperature:0.5}")
    @Getter
    private Double temperature;

    @Value("${spring.ai.ollama.max-tokens:4096}")
    @Getter
    private Integer maxTokens;

    @Value("${spring.ai.ollama.top-k:40}")
    @Getter
    private Integer topKModel;

    @Value("${spring.ai.ollama.top-p:0.9}")
    @Getter
    private Double topP;

    @Value("${spring.ai.ollama.min-p:0.0}")
    @Getter
    private Double minP;

    @Value("${spring.ai.ollama.frequency-penalty:0.0}")
    @Getter
    private Double frequencyPenalty;

    @Value("${spring.ai.ollama.presence-penalty:0.0}")
    @Getter
    private Double presencePenalty;

    @Value("${spring.ai.ollama.mirostat:0}")
    @Getter
    private Integer mirostat;

    @Value("${spring.ai.ollama.mirostat-eta:0.1}")
    @Getter
    private Double mirostatEta;

    @Value("${spring.ai.ollama.mirostat-tau:5.0}")
    @Getter
    private Double mirostatTau;

    @Value("${spring.ai.ollama.repeat-last-n:64}")
    @Getter
    private Integer repeatLastN;

    @Value("${spring.ai.ollama.repeat-penalty:1.1}")
    @Getter
    private Double repeatPenalty;

    @Value("${spring.ai.ollama.use-mmap:true}")
    @Getter
    private Boolean useMmap;

    @Value("${spring.ai.ollama.use-mlock:true}")
    @Getter
    private Boolean useMlock;

    @Value("${spring.ai.ollama.format:}")
    @Getter
    private String format;

    @Value("${spring.ai.ollama.num-keep:24}")
    @Getter
    private Integer numKeep;

    @Value("${spring.ai.ollama.num-ctx:8192}")
    @Getter
    private Integer numCtx;

    @Value("${spring.ai.ollama.num-batch:512}")
    @Getter
    private Integer numBatch;

    @Value("${spring.ai.ollama.num-thread:2}")
    @Getter
    private Integer numThread;

    @Value("${spring.ai.ollama.num-gpu:99}")
    @Getter
    private Integer numGpu;

    @Value("${spring.ai.ollama.keep-alive:5m}")
    @Getter
    private String keepAlive;

    private final OllamaApi ollamaApi;
    private BotConfigDTO defaultConfig;
    private final BotConfigRepository repo;

    @PostConstruct
    public void init() {
        try {
            systemPrompt = Files.readString(Path.of(systemPromptPath));
        } catch (IOException e) {
            log.error("incorrect system prompt path: {}, bot won't have base system prompt", systemPromptPath);
            systemPrompt = "";
        }
        try {
            ragPrompt = Files.readString(Path.of(ragPromptPath));
        } catch (IOException e) {
            log.error("incorrect rag prompt path: {}, bot won't have rag system prompt", ragPromptPath);
            ragPrompt = "";
        }
        defaultConfig = getInMemoryConfig();
    }

    private BotConfigDTO getInMemoryConfig() {
        return BotConfigDTO.builder()
                .botName(chatModelName)
                .systemPrompt(systemPrompt)
                .ragPrompt(ragPrompt)
                .chatMemoryMessages(chatMemoryMaxMessages)
                .topK(topK)
                .ignoreAuthorities(ignoreAuthorities)
                .similarityThreshold(similarityThreshold)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .topKModel(topKModel)
                .topP(topP)
                .minP(minP)
                .frequencyPenalty(frequencyPenalty)
                .presencePenalty(presencePenalty)
                .mirostat(mirostat)
                .repeatLastN(repeatLastN)
                .repeatPenalty(repeatPenalty)
                .useMmap(useMmap)
                .useMlock(useMlock)
                .format(format)
                .numKeep(numKeep)
                .numCtx(numCtx)
                .numBatch(numBatch)
                .numThread(numThread)
                .numGpu(numGpu)
                .keepAlive(keepAlive)
                .build();
    }

    public OllamaOptions getOllamaOptions() {
        OllamaOptions.Builder builder = OllamaOptions.builder()
                .model(getChatModelName())
                .temperature(getTemperature())
                .numPredict(getMaxTokens())
                .topK(getTopKModel())
                .topP(getTopP())
                .minP(getMinP())
                .mirostat(getMirostat())
                .repeatLastN(getRepeatLastN())
                .repeatPenalty(getRepeatPenalty())
                .frequencyPenalty(getFrequencyPenalty())
                .presencePenalty(getPresencePenalty())
                .numCtx(getNumCtx())
                .numBatch(getNumBatch())
                .numThread(getNumThread())
                .useMMap(getUseMmap())
                .useMLock(getUseMlock())
                .format(getFormat())
                .numKeep(getNumKeep())
                .numGPU(getNumGpu())
                .keepAlive(getKeepAlive());

        return builder.build();
    }

    private void setInMemoryConfig(BotConfigDTO dto,
                                   CustomQuestionAnswerAdvisor questionAnswerAdvisor,
                                   MessageCustomChatMemory chatMemory) throws IllegalArgumentException {
        setChatModelName(dto.getBotName());
        setChatMemoryMaxMessages(dto.getChatMemoryMessages(), chatMemory);
        setSystemPrompt(dto.getSystemPrompt());
        setRagPrompt(dto.getRagPrompt(), questionAnswerAdvisor);
        setTopK(dto.getTopK());
        setIgnoreAuthorities(dto.isIgnoreAuthorities());
        setSimilarityThreshold(dto.getSimilarityThreshold());
        setTemperature(dto.getTemperature());
        setMaxTokens(dto.getMaxTokens());
        setTopKModel(dto.getTopKModel());
        setTopP(dto.getTopP());
        setMinP(dto.getMinP());
        setFrequencyPenalty(dto.getFrequencyPenalty());
        setPresencePenalty(dto.getPresencePenalty());
        setMirostat(dto.getMirostat());
        setRepeatLastN(dto.getRepeatLastN());
        setRepeatPenalty(dto.getRepeatPenalty());
        setUseMmap(dto.getUseMmap());
        setUseMlock(dto.getUseMlock());
        setFormat(dto.getFormat());
        setNumKeep(dto.getNumKeep());
        setNumCtx(dto.getNumCtx());
        setNumBatch(dto.getNumBatch());
        setNumThread(dto.getNumThread());
        setNumGpu(dto.getNumGpu());
        setKeepAlive(dto.getKeepAlive());
    }

    public void saveConfig(BotConfigDTO dto,
                           CustomQuestionAnswerAdvisor questionAnswerAdvisor,
                           MessageCustomChatMemory chatMemory)
            throws IllegalArgumentException {
        var prevConfig = getInMemoryConfig();
        try {
            setInMemoryConfig(dto, questionAnswerAdvisor, chatMemory);
        } catch (IllegalArgumentException e) {
            // Одна из настроек может отвалиться по IllegalArgumentException, отменяем тогда все обновления,
            // Восстанавливаем последнее
            setInMemoryConfig(prevConfig, questionAnswerAdvisor, chatMemory);
            throw new IllegalArgumentException(e.getMessage());
        }

        BotConfigEntity e = new BotConfigEntity();
        e.setId(1L);
        e.setConfig(getInMemoryConfig());
        e.setUpdatedAt(System.currentTimeMillis());
        repo.save(e);
    }

    public BotConfigDTO getConfig() {
        return repo.findById(1L)
                .orElseGet(this::createDefault)
                .getConfig();
    }

    public void resetConfig(CustomQuestionAnswerAdvisor questionAnswerAdvisor,
                            MessageCustomChatMemory chatMemory) throws IllegalArgumentException {
        saveConfig(defaultConfig, questionAnswerAdvisor, chatMemory);
    }

    private BotConfigEntity createDefault() {
        BotConfigEntity e = new BotConfigEntity();
        e.setId(1L);
        e.setConfig(defaultConfig);
        e.setUpdatedAt(System.currentTimeMillis());
        return repo.save(e);
    }

    public SearchRequest searchRequest(){
        return SearchRequest.builder()
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build();
    }

    private void setChatMemoryMaxMessages(int chatMemoryMaxMessages, MessageCustomChatMemory chatMemory)
            throws IllegalArgumentException {
        if (chatMemoryMaxMessages < 0) {
            throw new IllegalArgumentException("chatMemoryMaxMessages cannot be negative");
        }
        if (chatMemoryMaxMessages % 2 != 0) {
            throw new IllegalArgumentException("chatMemoryMaxMessages must be an even number");
        }
        this.chatMemoryMaxMessages = chatMemoryMaxMessages;
        chatMemory.setMaxMessages(chatMemoryMaxMessages);
    }

    private void setChatModelName(String modelName) {
        if (ollamaApi.listModels().models().stream().anyMatch(model1 -> model1.name().equals(modelName)))
            this.chatModelName = modelName;
    }

    private void setRagPrompt(String ragPrompt, CustomQuestionAnswerAdvisor questionAnswerAdvisor)
            throws IllegalArgumentException {
        questionAnswerAdvisor.setRagPrompt(ragPrompt);
        this.ragPrompt = ragPrompt;
    }

    private void setSimilarityThreshold(double similarityThreshold) throws IllegalArgumentException {
        if (similarityThreshold < 0 || similarityThreshold > 1) {
            throw new IllegalArgumentException("similarityThreshold must be between 0 and 1");
        }
        this.similarityThreshold = similarityThreshold;
    }

    private void setTopK(int topK) {
        if (topK < 0) {
            throw new IllegalArgumentException("topK cannot be negative");
        }
        this.topK = topK;
    }

    private void setTemperature(Double temperature) throws IllegalArgumentException {
        if (temperature != null && (temperature < 0 || temperature > 2)) {
            throw new IllegalArgumentException("temperature must be between 0 and 2");
        }
        this.temperature = temperature;
    }

    private void setMaxTokens(Integer maxTokens) throws IllegalArgumentException {
        if (maxTokens != null && maxTokens < 1) {
            throw new IllegalArgumentException("maxTokens must be greater than 0");
        }
        this.maxTokens = maxTokens;
    }

    private void setTopKModel(Integer topKModel) throws IllegalArgumentException {
        if (topKModel != null && topKModel < 0) {
            throw new IllegalArgumentException("topKModel cannot be negative");
        }
        this.topKModel = topKModel;
    }

    private void setTopP(Double topP) throws IllegalArgumentException {
        if (topP != null && (topP < 0 || topP > 1)) {
            throw new IllegalArgumentException("topP must be between 0 and 1");
        }
        this.topP = topP;
    }

    private void setMinP(Double minP) throws IllegalArgumentException {
        if (minP != null && (minP < 0 || minP > 1)) {
            throw new IllegalArgumentException("minP must be between 0 and 1");
        }
        this.minP = minP;
    }

    private void setFrequencyPenalty(Double frequencyPenalty) throws IllegalArgumentException {
        if (frequencyPenalty != null && (frequencyPenalty < -2 || frequencyPenalty > 2)) {
            throw new IllegalArgumentException("frequencyPenalty must be between -2 and 2");
        }
        this.frequencyPenalty = frequencyPenalty;
    }

    private void setPresencePenalty(Double presencePenalty) throws IllegalArgumentException {
        if (presencePenalty != null && (presencePenalty < -2 || presencePenalty > 2)) {
            throw new IllegalArgumentException("presencePenalty must be between -2 and 2");
        }
        this.presencePenalty = presencePenalty;
    }

    private void setMirostat(Integer mirostat) throws IllegalArgumentException {
        if (mirostat != null && (mirostat < 0 || mirostat > 2)) {
            throw new IllegalArgumentException("mirostat must be between 0 and 2");
        }
        this.mirostat = mirostat;
    }

    private void setRepeatLastN(Integer repeatLastN) throws IllegalArgumentException {
        if (repeatLastN != null && repeatLastN < -1) {
            throw new IllegalArgumentException("repeatLastN cannot be less than -1");
        }
        this.repeatLastN = repeatLastN;
    }

    private void setRepeatPenalty(Double repeatPenalty) throws IllegalArgumentException {
        if (repeatPenalty != null && (repeatPenalty < -2 || repeatPenalty > 2)) {
            throw new IllegalArgumentException("repeatPenalty must be between -2 and 2");
        }
        this.repeatPenalty = repeatPenalty;
    }

    private void setUseMmap(Boolean useMmap) {
        this.useMmap = useMmap;
    }

    private void setUseMlock(Boolean useMlock) {
        this.useMlock = useMlock;
    }

    private void setFormat(String format) {
        this.format = format;
    }

    private void setNumKeep(Integer numKeep) throws IllegalArgumentException {
        if (numKeep != null && numKeep < -1) {
            throw new IllegalArgumentException("numKeep cannot be less than -1");
        }
        this.numKeep = numKeep;
    }

    private void setNumCtx(Integer numCtx) throws IllegalArgumentException {
        if (numCtx != null && numCtx < -1) {
            throw new IllegalArgumentException("numCtx cannot be less than -1");
        }
        this.numCtx = numCtx;
    }

    private void setNumBatch(Integer numBatch) throws IllegalArgumentException {
        if (numBatch != null && numBatch < 256) {
            throw new IllegalArgumentException("numBatch must be at least 256");
        }
        this.numBatch = numBatch;
    }

    private void setNumThread(Integer numThread) throws IllegalArgumentException {
        if (numThread != null && numThread < 1) {
            throw new IllegalArgumentException("numThread must be at least 1");
        }
        this.numThread = numThread;
    }

    private void setNumGpu(Integer numGpu) throws IllegalArgumentException {
        if (numGpu != null && numGpu < 0) {
            throw new IllegalArgumentException("numGpu cannot be negative");
        }
        this.numGpu = numGpu;
    }

    private void setKeepAlive(String keepAlive) {
        this.keepAlive = keepAlive;
    }
}