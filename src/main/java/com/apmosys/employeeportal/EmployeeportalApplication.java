package com.apmosys.employeeportal;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

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
		 
		 System.out.println(LoggerFactory.getILoggerFactory().getClass().getName());
	        return new RestTemplate();
	    }
	 
//	 @Bean
//	    public FilterRegistrationBean<RequestValidationFilter> requestValidationFilter(RequestValidationFilter filter) {
//	        FilterRegistrationBean<RequestValidationFilter> registrationBean = new FilterRegistrationBean<>();
//	        registrationBean.setFilter(filter);
//	        registrationBean.addUrlPatterns("/*"); // apply to all endpoints
//	        registrationBean.setOrder(1); // ensure it runs early
//	        return registrationBean;
//	    }
}
