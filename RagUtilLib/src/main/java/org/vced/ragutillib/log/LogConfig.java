package org.vced.ragutillib.log;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan(basePackages = {"org.vced.ragutillib.log"})
@ComponentScan(basePackages = {"org.vced.ragutillib.log"})
public class LogConfig {

}
