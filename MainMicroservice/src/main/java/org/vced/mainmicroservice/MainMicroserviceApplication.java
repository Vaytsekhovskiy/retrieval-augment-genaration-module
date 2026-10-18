package org.vced.mainmicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.vced.ragutillib.log.LogConfig;

import java.util.concurrent.Executor;

@SpringBootApplication // здесь было (exclude = {DataSourceAutoConfiguration.class})
@Import(value = {LogConfig.class})
@EnableAsync
public class MainMicroserviceApplication {
	public static void main(String[] args) {
		SpringApplication.run(MainMicroserviceApplication.class, args);
	}
	/** Для ассинхроности. Т.е. есть какой то список, работающий как очередь, из которого достаются треды(нити)
	 * Executor — базовый интерфейс для классов, который реализует запуск Runnable задач. Тем самым обеспечивается помощь с добавлением задачи и способом ее запуска.
	 * Класс для упарвления и монитринга некоторых полезных атрибутов: "corePoolSize",  "corePoolSize", "maxPoolSize", "keepAliveSeconds" (all supporting updates at runtime); "poolSize", "activeCount" (for introspection only)
	 * Ставим минимальное количество рабочих, которые неперерывно работают
	 * Ставим максимальное количество рабочих(потоков) возможное для работы
	 * *Ставим размер очереди
	 * Префикс для потока
	 *Финальное действие - инициализируем его
	 *  */
	@Bean
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("GithubLookup-");
		executor.initialize();
		return executor;
	}
}
