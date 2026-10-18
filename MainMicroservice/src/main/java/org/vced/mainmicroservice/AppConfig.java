package org.vced.mainmicroservice;

import freemarker.ext.beans.BeansWrapper;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.HashMap;
import java.util.Map;

/**@author Егор
 * Конфиг для добавления экземляров в бины*/
@Configuration
@PropertySource("classpath:application.yml")
public class AppConfig implements BeanPostProcessor {
    @Value("${microservice.gateway}")
    public String BASE_URL;
    @Value("${youtrack.api.base-url}")
    public String youtrackBaseUrl;
    @Value("${microservice.internal}")
    public String INTERNAL_BASE_URL;

    @Bean
    public WebClient webClient() {
        // 10 MB
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10 MB
                        .build()
                )
                .baseUrl(INTERNAL_BASE_URL)
                .build();
    }


    @SneakyThrows         // or use try / catch (if not using lombok)
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FreeMarkerConfigurer) {
            FreeMarkerConfigurer configurer = (FreeMarkerConfigurer) bean;
            freemarker.template.Configuration configuration = configurer.getConfiguration();
            BeansWrapper objectWrapper = (BeansWrapper) configuration.getObjectWrapper();
            configuration.setSharedVariables(getSharedVariables(objectWrapper));
        }
        return bean;
    }

    private Map<String, ?> getSharedVariables(BeansWrapper config) {
        // Получаем значение BASE_URL из AppConfig
        String baseUrl = BASE_URL;

        // Добавляем переменную в модель FreeMarker
        Map<String, Object> sharedVariables = new HashMap<>();
        sharedVariables.put("gatewayUrl", baseUrl);
        sharedVariables.put("youTrackUrl", youtrackBaseUrl);
        return sharedVariables;
    }
}