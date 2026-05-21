package com.medhelp.medhelp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
/**
 * Entry point for PathLab SaaS.
 *
 * @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
 * @EnableScheduling      = activates @Scheduled methods (TAT alerts, subscription checks)
 */
@SpringBootApplication
@EnableScheduling
public class MedhelpApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedhelpApplication.class, args);
	}

}
