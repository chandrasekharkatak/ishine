package com.apmosys.employeeportal;

import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(exclude = { TaskSchedulingAutoConfiguration.class })
public class ShankhGatewayApplication {

	public static void main(String[] args) {
		System.out.println("========================================");
	    System.out.println("  Starting ShankhGateway Application...");
	    System.out.println("========================================");

		SpringApplication.run(ShankhGatewayApplication.class, args);
		
		System.out.println("========================================");
        System.out.println("  ShankhGateway Application is RUNNING!");
        System.out.println("========================================");
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	public RestTemplate restTemplate() {
		System.out.println(LoggerFactory.getILoggerFactory().getClass().getName());
		return new RestTemplate();
	}
}
