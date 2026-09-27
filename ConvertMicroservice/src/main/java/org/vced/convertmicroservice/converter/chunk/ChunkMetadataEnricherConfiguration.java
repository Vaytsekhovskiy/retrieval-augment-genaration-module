package org.vced.convertmicroservice.converter.chunk;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ChunkMetadataEnricherConfiguration {
    private final ChatModel chatModel; //OllamaChatModel
    /*
        TODO:
            В конечном итоге избавился от поиска ключевых слов, т.к.
            1. Оно делает это хреново
            2. Нельзя поменять промпт по умолчанию (public static final String KEYWORDS_TEMPLATE)
            3. Потребляет много ресурсов
            4. из-за нехватки ресурсов, можно ставить только маленькую модель, не имеет смысла
            ГЛАВНАЯ ПРИЧИНА: хреновая модель из-за нехватки ресов => хреновые ключ. слова
        @Bean
        KeywordMetadataEnricher keywordMetadataEnricher(){ // 5 ???
            var keywordEnricher = new KeywordMetadataEnricher(chatModel, 5);
            return keywordEnricher;
        }
     */
    @Bean
    SummaryMetadataEnricher summaryMetadataEnricher(){
        return new SummaryMetadataEnricher(
                chatModel,
                List.of(
                //SummaryMetadataEnricher.SummaryType.PREVIOUS,
                //SummaryMetadataEnricher.SummaryType.NEXT,
                SummaryMetadataEnricher.SummaryType.CURRENT
                ),
                """
							Here is the content of the section:
							{context_str}
							
							Summarize the key topics and entities of the section.
							Write everything in one sentence, in the same language as the content of the section.
							
							Summary:""",
                MetadataMode.ALL
        );
    }
}
