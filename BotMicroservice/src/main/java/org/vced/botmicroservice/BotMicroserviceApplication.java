package org.vced.botmicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.vced.ragutillib.log.LogConfig;

@SpringBootApplication
@EnableAsync
@Import(value = {LogConfig.class})
public class BotMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotMicroserviceApplication.class, args);
        /*
        *Запускаем работу микросервиса
        */
    }
}