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
		SpringApplication.run(ShankhGatewayApplication.class, args);
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
