package com.intern.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(@Value("${userservice.baseurl}") String usersBaseUrl) {
        return WebClient.builder()
                .baseUrl(usersBaseUrl)
//                .filter(setJWT())
                .build();
    }

//    private ExchangeFilterFunction setJWT() {
//        return ExchangeFilterFunction.ofRequestProcessor(clientRequest ->
//                ReactiveSecurityContextHolder.getContext()
//                        .map(securityContext -> {
//                            Authentication auth = securityContext.getAuthentication();
//                            if (auth != null) {
//                                if (auth.getCredentials() instanceof String) {
//                                    String token = (String) auth.getCredentials();
//                                    return ClientRequest.from(clientRequest)
//                                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
//                                            .build();
//                                }
//                            }
//                            return clientRequest;
//                        })
//        );
//    }
}
