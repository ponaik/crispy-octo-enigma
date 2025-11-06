package com.intern.orderservice.service;

import com.intern.orderservice.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserApiService {

    public static final String USERS_ENDPOINT = "/users/";
    private final RestTemplate restTemplate;

    @Value("${userservice.baseurl}")
    private String userServiceUrl;

    @Autowired
    public UserApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UserResponse getUserById(Long userId) {
        String url = userServiceUrl + USERS_ENDPOINT + userId;
        ResponseEntity<UserResponse> response = restTemplate.getForEntity(url, UserResponse.class);
        return response.getBody();
    }
}

