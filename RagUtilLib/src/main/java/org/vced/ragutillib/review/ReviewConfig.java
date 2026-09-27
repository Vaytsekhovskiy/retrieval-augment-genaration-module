package org.vced.ragutillib.review;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages ={"org.vced.ragutillib.review"})
@EntityScan(basePackages = {"org.vced.ragutillib.review"})
@ComponentScan(basePackages = {"org.vced.ragutillib.review"})
public class ReviewConfig {

}