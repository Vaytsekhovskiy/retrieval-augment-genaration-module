package org.vced.ragutillib.authority;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"org.vced.ragutillib.authority"})
@EntityScan(basePackages = {"org.vced.ragutillib.authority"})
@ComponentScan(basePackages = {"org.vced.ragutillib.authority"})
public class AuthorityMappingConfig {
}
