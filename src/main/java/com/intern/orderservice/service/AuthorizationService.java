package com.intern.orderservice.service;

public interface AuthorizationService {
    String ADMIN = "ROLE_admin";
    String USER = "ROLE_user";
    String CLAIM = "email";

    boolean isAdmin();

    boolean isUser();

    String getEmail();
}
