package org.vced.convertmicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.vced.ragutillib.log.LogConfig;

@SpringBootApplication(scanBasePackages = "org.vced.convertmicroservice")
@EnableScheduling
@Import(value = {LogConfig.class})
@EnableJpaRepositories(basePackages = "org.vced.convertmicroservice")
@EntityScan(basePackages = "org.vced.convertmicroservice")
public class ConvertMicroserviceApplication {
	public static void main(String[] args) {
		SpringApplication.run(ConvertMicroserviceApplication.class, args);
	}

}
