package com.medhelp.common.medhelp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
/**
 * Entry point for PathLab SaaS.
 *
 * @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
 * @EnableScheduling      = activates @Scheduled methods (TAT alerts, subscription checks)
 */
@SpringBootApplication(scanBasePackages = "com.medhelp")
@EnableJpaRepositories(basePackages = "com.medhelp")
@EntityScan(basePackages = "com.medhelp")
@EnableScheduling
public class MedhelpApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedhelpApplication.class, args);
	}

}
