package org.vced.ragutillib.vector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
@Slf4j
@Configuration
@EnableJpaRepositories(basePackages ={"org.vced.ragutillib.vector"})
@EntityScan(basePackages = {"org.vced.ragutillib.vector"})
@ComponentScan(basePackages = {"org.vced.ragutillib.vector"})
@ConfigurationProperties(prefix = "embedding-model")
public class VectorConfig {
}
