package com.apmosys.employeeportal;

import java.time.LocalDateTime;

import javax.servlet.MultipartConfigElement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import com.apmosys.employeeportal.EmployeePortalInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class MyConfig implements WebMvcConfigurer{

	@Autowired
	private EmployeePortalInterceptor employeePortalInterceptor;
	
	@Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
        		.allowedOrigins("*")
        		.allowedMethods("POST","GET","PUT","DELETE", "OPTIONS")
        		.allowedHeaders("Content-Type", "Accept", "X-Requested-With", "loader", "Authorization", "X-FORWARDED-FOR","sw8")
				.exposedHeaders("Content-Type", "Accept", "X-Requested-With", "loader", "Authorization", "X-FORWARDED-FOR","sw8");
        }
	
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(employeePortalInterceptor);
	}
	
	@Bean
	public MultipartConfigElement multipartConfigElement() {
	    MultipartConfigFactory factory = new MultipartConfigFactory();
//	    factory.setMaxFileSize(DataSize.of(10, DataUnit.MEGABYTES));
//	    factory.setMaxRequestSize(DataSize.of(10, DataUnit.MEGABYTES));
	    return factory.createMultipartConfig();
	}
	
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.
            csrf().disable().
            headers().frameOptions().deny();
            return http.build();
    }
	
}
