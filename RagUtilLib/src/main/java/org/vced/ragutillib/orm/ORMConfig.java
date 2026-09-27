package org.vced.ragutillib.orm;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan(basePackages = {"org.vced.ragutillib.orm"})
@ComponentScan(basePackages = {"org.vced.ragutillib.orm"})
public class ORMConfig {

}