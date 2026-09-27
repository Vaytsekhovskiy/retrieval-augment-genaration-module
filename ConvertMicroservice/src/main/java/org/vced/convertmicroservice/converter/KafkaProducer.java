package org.vced.convertmicroservice.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.vced.convertmicroservice.watcher.FilePathAbstractService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**@author  Егор
 * Класс брокер отвечающий за отправку сообщений*/
@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    /**Метод отправки сообщения*/
    public CompletableFuture<Void> sendMessage(Map.Entry<String, FilePathAbstractService.FILE_STATE> sentFile){
        CompletableFuture<Void> future =
                kafkaTemplate.send("filesCheck", sentEntry2json(sentFile))
                        .thenApply(result -> null); // Результат нам не нужен
        kafkaTemplate.flush();

        return future;
    }
    public static String sentEntry2json(Map.Entry<String, FilePathAbstractService.FILE_STATE> sentFile){
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonSentFile = sentFile.getKey();
        try {
            jsonSentFile = objectMapper.writeValueAsString(sentFile);
        } catch (JsonProcessingException e ){}
        return jsonSentFile;
    }
}
