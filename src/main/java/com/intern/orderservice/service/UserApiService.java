package com.intern.orderservice.service;

import com.intern.orderservice.dto.response.UserResponse;

public interface UserApiService {

    UserResponse getUserById(Long userId);

    UserResponse getUserByEmail(String email);

    UserResponse getUserByIdFallback(Long userId, RuntimeException t);
}
