package com.intern.orderservice.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate jwtPropagatingRestTemplate(HttpServletRequest request) {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getInterceptors().add((httpRequest, body, execution) -> {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null) {
                httpRequest.getHeaders().add("Authorization", authHeader);
            }
            return execution.execute(httpRequest, body);
        });

        return restTemplate;
    }
}
