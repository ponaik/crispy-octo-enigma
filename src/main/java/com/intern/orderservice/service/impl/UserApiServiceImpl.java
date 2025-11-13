package com.intern.orderservice.service.impl;

import com.intern.orderservice.dto.response.UserResponse;
import com.intern.orderservice.service.UserApiService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class UserApiServiceImpl implements UserApiService {

    private final RestTemplate restTemplate;
    private final String USERS_SEARCH_ENDPOINT;
    private final String USERS_ENDPOINT;
    private final String USERSERVICE_URL;

    @Autowired
    public UserApiServiceImpl(
            RestTemplate restTemplate,
            @Value("${userservice.endpoint.search-email}") String USERS_SEARCH_ENDPOINT,
            @Value("${userservice.endpoint.users}") String USERS_ENDPOINT,
            @Value("${userservice.baseurl}") String USERSERVICE_URL
    ) {
        this.restTemplate = restTemplate;
        this.USERS_SEARCH_ENDPOINT = USERS_SEARCH_ENDPOINT;
        this.USERS_ENDPOINT = USERS_ENDPOINT;
        this.USERSERVICE_URL = USERSERVICE_URL;
    }

    @CircuitBreaker(name = "UserService", fallbackMethod = "getUserByIdFallback")
    @Override
    public UserResponse getUserById(Long userId) {
        String url = USERSERVICE_URL + USERS_ENDPOINT + userId;
        ResponseEntity<UserResponse> response = restTemplate.getForEntity(url, UserResponse.class);

        log.info("Response from UserService by id: {}, {}", response.getStatusCode(), response.getBody());
        return response.getBody();
    }

    @CircuitBreaker(name = "UserService")
    @Override
    public UserResponse getUserByEmail(String email) {
        String url = USERSERVICE_URL + USERS_SEARCH_ENDPOINT + email;
        ResponseEntity<UserResponse> response = restTemplate.getForEntity(url, UserResponse.class);
        log.info("Response from UserService by email: {}, {}", response.getStatusCode(), response.getBody());
        return response.getBody();
    }

    @Override
    public UserResponse getUserByIdFallback(Long userId, RuntimeException t) {
        if (t instanceof HttpClientErrorException http) {
            log.info("Circuit breaker fallback response for userId: {}, {}", userId, http.getStatusCode());
            if (http.getStatusCode() == HttpStatus.NOT_FOUND) {
                // return placeholder NotFound UserResponse so admin can see orders with nonexisting users
                return new UserResponse(null, HttpStatus.NOT_FOUND.toString(), null, null, null);
            }
        }
        throw t;
    }
}

