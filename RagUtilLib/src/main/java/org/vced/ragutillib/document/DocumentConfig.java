package org.vced.ragutillib.document;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages ={"org.vced.ragutillib.document"})
@EntityScan(basePackages = {"org.vced.ragutillib.document"})
@ComponentScan(basePackages = {"org.vced.ragutillib.document"})
public class DocumentConfig {

}