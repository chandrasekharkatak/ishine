package com.apmosys.employeeportal;

import javax.servlet.MultipartConfigElement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.apmosys.employeeportal.EmployeePortalInterceptor;

@Configuration
@EnableWebSecurity
public class MyConfig implements WebMvcConfigurer {

    @Autowired
    private EmployeePortalInterceptor employeePortalInterceptor;
    
    @Value("${security.csp.policy}")
    private String contentSecurityPolicy;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("POST", "GET", "PUT")
//                .allowedMethods("POST", "GET", "PUT" ,"DELETE")
                .allowedHeaders("Content-Type", "Accept", "X-Requested-With", "loader",
                        "Authorization", "X-FORWARDED-FOR", "Sw8", "X-TRACE-MAP")
                .exposedHeaders("Content-Type", "Accept", "X-Requested-With", "loader",
                        "Authorization", "X-FORWARDED-FOR", "Sw8", "X-TRACE-MAP");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(employeePortalInterceptor);
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
//        factory.setMaxFileSize(DataSize.of(10, DataUnit.MEGABYTES));
//        factory.setMaxRequestSize(DataSize.of(10, DataUnit.MEGABYTES));
        return factory.createMultipartConfig();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(request -> {
                        String method = request.getMethod();
                        return false;
                        //return method.equals("TRACE") || method.equals("DEBUG") || method.equals("DELETE");
//                        return method.equals("TRACE") || method.equals("DEBUG") ;

                    }).denyAll()
                    .anyRequest().permitAll()
                )
            
            .headers(headers -> headers
                // Allow iframe embedding from external sites (controlled by CSP frame-ancestors).
            		.frameOptions(frame -> frame.disable())


                // Strict-Transport-Security
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                    .preload(true)
                )

                .contentSecurityPolicy(csp -> csp
                		.policyDirectives(contentSecurityPolicy)        
                )

                // Referrer-Policy
                .referrerPolicy(referrer ->
                    referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
                )
             // ✅ Add extra security headers here, not inside PermissionsPolicyConfig
                .addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Resource-Policy", "same-origin"))
                .addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Opener-Policy", "same-origin"))
                .addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Embedder-Policy", "require-corp"))
                // Permissions-Policy
                .permissionsPolicy(policy ->
                    policy.policy("geolocation=(), microphone=(), camera=()")
                )
            );

        return http.build();
    }
}
