package org.vced.apigateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.vced.ragutillib.authentication.CryptoUtils;
import org.vced.ragutillib.orm.AuthenticationDataORM;
import org.vced.ragutillib.orm.ORMConfig;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
@Import(value = {ORMConfig.class})
public class AuthenticationGatewayFilter extends AbstractGatewayFilterFactory<AuthenticationGatewayFilter.Config> {
    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    public AuthenticationGatewayFilter() {
        super(Config.class);
    }

    public static class Config {
        // Конфигурация фильтра (если нужна)
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            try {
                String XUser = exchange.getRequest().getQueryParams().getFirst("x-user");
                if (XUser == null) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User should be logged in");
                }
                XUser = CryptoUtils.decrypt(XUser);
                AuthenticationDataORM authenticationDataORM = objectMapper
                        .readValue(XUser, AuthenticationDataORM.class);
                if (authenticationDataORM.getName() == null )
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User should be logged in");
                log.debug("User: \n{}\nURI: \n{}\nURI_length: \n{}",
                        authenticationDataORM, exchange.getRequest().getURI(),
                        exchange.getRequest().getURI().toString().getBytes(StandardCharsets.UTF_8).length);
            } catch (Exception e) {
                log.error("Unauthorized attempt inside AuthenticationGatewayFilter, URL: {}",
                        exchange.getRequest().getURI(), e);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User should be logged in");
            }

            // Перехват ответа перед отправкой на фронт
            return chain.filter(exchange).then(
                    Mono.fromRunnable(() -> {
                        var headers = exchange.getResponse().getHeaders();
                        List<String> origins = headers.get("Access-Control-Allow-Origin");
                        if (origins != null && origins.size() > 1) {
                            // Оставляем только один (первый) заголовок
                            headers.set("Access-Control-Allow-Origin", origins.getFirst());
                        }
                    })
            );
        };
    }

}
