package org.vced.apigateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.vced.ragutillib.log.LogConfig;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@Import(value = {LogConfig.class})
@PropertySource("classpath:application.yml")
@Configuration
@Slf4j
public class ApiGatewayApplication {
    @Value("${server.port}")
    public String port;
    @Value("${eureka.instance.hostname}")
    public String host;

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder,
                                 AuthenticationGatewayFilter authFilter) {
        String selfUri = String.format("http://%s:%s", host, port);

        // Конфигурация для auth фильтра
        AuthenticationGatewayFilter.Config authConfig = new AuthenticationGatewayFilter.Config();

        return builder.routes()
                .route(p -> p
                        .path("/api/search/**")
                        .filters(f -> f
                                .filter(authFilter.apply(authConfig))
                                .rewritePath("/(?<segment>.*)", "/bot/${segment}"))
                        .uri(selfUri))
                .route(p -> p
                        .path("/api/bot/**")
                        .filters(f -> f
                                .filter(authFilter.apply(authConfig))
                                .rewritePath("/(?<segment>.*)", "/bot/${segment}"))
                        .uri(selfUri))
                .route(p -> p
                        .path("/api/convert/**")
                        .filters(f -> f
                                .filter(authFilter.apply(authConfig))
                                .rewritePath("/(?<segment>.*)", "/convert/${segment}"))
                        .uri(selfUri))
                .route(p -> p
                        .path("/api/main/**")
                        .filters(f -> f
                                .filter(authFilter.apply(authConfig))
                                .rewritePath("/(?<segment>.*)", "/main/${segment}"))
                        .uri(selfUri))
                .build();
    }
    @Bean
    public NettyServerCustomizer nettyServerCustomizer() {
        log.info("trying to configure NettyServerCustomizer with 32768 params");
        return httpServer -> httpServer
                .httpRequestDecoder(spec -> spec
                        .maxInitialLineLength(32768)
                        .maxHeaderSize(32768)
                );
    }
    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> nettyCustomizer() {
        log.info("trying to configure WebServerFactoryCustomizer<NettyReactiveWebServerFactory> with 32768 params");
        return factory -> factory.addServerCustomizers(httpServer ->
                httpServer.httpRequestDecoder(spec -> spec
                        .maxInitialLineLength(32768)
                        .maxHeaderSize(32768)
                )
        );
    }
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
