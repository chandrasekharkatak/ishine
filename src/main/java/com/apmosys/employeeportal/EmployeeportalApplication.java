package com.apmosys.employeeportal;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication 
@EnableScheduling
public class EmployeeportalApplication {
	
	public static void main(String[] args) {
		     
		SpringApplication.run(EmployeeportalApplication.class, args);
	}
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
	 @Bean
	    public RestTemplate restTemplate() {
	        return new RestTemplate();
	    }
}
