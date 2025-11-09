package com.intern.orderservice.service;

import com.intern.orderservice.dto.response.UserResponse;
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
    public static final String USERS_SEARCH_ENDPOINT = "/users/search?email=";
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

        log.info("Response from UserService by id: {}, {}", response.getStatusCode(), response.getBody());
        return response.getBody();
    }

    @CircuitBreaker(name = "UserService")
    public UserResponse getUserByEmail(String email) {
        String url = userServiceUrl + USERS_SEARCH_ENDPOINT + email;
        ResponseEntity<UserResponse> response = restTemplate.getForEntity(url, UserResponse.class);
        log.info("Response from UserService by email: {}, {}", response.getStatusCode(), response.getBody());
        return response.getBody();
    }

    public UserResponse getUserByIdFallback(Long userId, RuntimeException t) {
        if (t instanceof HttpClientErrorException http) {
            log.info("Circuit breaker fallback response for userId: {}, {}", userId, http.getStatusCode());
            if (http.getStatusCode().value() == 404) {
                // return null so admin can see orders with nonexisting users
                return null;
            }
        }
        throw t;
    }
}

