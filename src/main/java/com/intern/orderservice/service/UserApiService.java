package com.intern.orderservice.service;

import com.intern.orderservice.dto.UserResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class UserApiService {

    public static final String USERS_ENDPOINT = "/users/";
    private final RestTemplate restTemplate;

    @Value("${userservice.baseurl}")
    private String userServiceUrl;

    @Autowired
    public UserApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "UserService", fallbackMethod = "getUserByIdFallback")
    public UserResponse getUserById(Long userId) {
        String url = userServiceUrl + USERS_ENDPOINT + userId;
        ResponseEntity<UserResponse> response = restTemplate.getForEntity(url, UserResponse.class);

        log.info("Response from UserService: {}, {}", response.getStatusCode(), response.getBody());
        return response.getBody();
    }

    public UserResponse getUserByIdFallback(Long userId, Throwable t) {
        if (t instanceof HttpClientErrorException http) {
            log.info("Fallback response for userId: {}, {}", userId, http.getStatusCode());
            if (http.getStatusCode().value() == 404) {
                // return null or an empty/sentinel response for not found
                return null;
            }
            if (http.getStatusCode().value() == 401) {
                // return an auth-specific sentinel or rethrow/wrap as needed
                return null;
            }
        }

        // generic fallback (could rethrow or return cached/default)
        return null;
    }
}

