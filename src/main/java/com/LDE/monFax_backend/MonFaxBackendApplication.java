package com.LDE.monFax_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MonFaxBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(MonFaxBackendApplication.class, args);
	}
}
