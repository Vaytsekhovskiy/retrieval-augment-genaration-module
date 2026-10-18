package org.vced.botmicroservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

@Converter
@Component
public class JsonMapConverter implements AttributeConverter<BotConfigDTO, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(BotConfigDTO object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BotConfigDTO convertToEntityAttribute(String json) {
        try {
            return mapper.readValue(json, BotConfigDTO.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
