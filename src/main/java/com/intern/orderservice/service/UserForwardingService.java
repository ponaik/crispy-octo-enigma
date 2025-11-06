package com.intern.orderservice.service;

import com.intern.orderservice.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class UserForwardingService {

    private final WebClient usersWebClient;

    public UserForwardingService(WebClient usersWebClient) {
        this.usersWebClient = usersWebClient;
    }

    /**
     * Forward the raw Authorization header value (e.g. "Bearer <token>") to the users service.
     * Returns Mono.empty() on 404, propagates other errors wrapped as runtime exceptions.
     */
    public Mono<UserResponse> getUserById(String userId, String authorizationHeader) {



        return usersWebClient.get()
                .uri("/users/{id}", userId)
                .headers(h -> {
                    if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                        h.set("Authorization", authorizationHeader);
                    }
                })
                .retrieve()
                .bodyToMono(UserResponse.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> Mono.empty())
                .onErrorMap(ex -> new RuntimeException("users service call failed", ex));
    }
}
