package com.pravinmali.orderprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Application entry point.
 *
 * @EnableJpaAuditing activates Spring Data JPA's auditing support so that
 * @CreatedDate and @LastModifiedDate fields on entities are automatically
 * populated without any manual timestamp management.
 */
@SpringBootApplication
@EnableJpaAuditing
public class OrderProcessingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderProcessingServiceApplication.class, args);
	}
}
