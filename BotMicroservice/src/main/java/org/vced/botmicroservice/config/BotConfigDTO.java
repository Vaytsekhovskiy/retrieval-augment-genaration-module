package org.vced.botmicroservice.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotConfigDTO {
    private String botName;
    private String systemPrompt;
    private String ragPrompt;
    private int chatMemoryMessages;
    // Embedding
    private int topK;
    private boolean ignoreAuthorities;
    private double similarityThreshold;

    // LLM Params
    private Double temperature;
    private Integer maxTokens;
    private Integer topKModel;
    private Double topP;
    private Double minP;
    private Double frequencyPenalty;
    private Double presencePenalty;
    private Integer mirostat;
    private Integer repeatLastN;
    private Double repeatPenalty;
    private Boolean useMmap;
    private Boolean useMlock;
    private String format;
    private Integer numKeep;
    private Integer numCtx;
    private Integer numBatch;
    private Integer numThread;
    private Integer numGpu;
    private String keepAlive;
}