package com.intern.orderservice.service.impl;

import com.intern.orderservice.service.AuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private Jwt getJwt() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt principal) {
            return principal;
        }
        return null;
    }

    @Override public boolean isAdmin() {
        Authentication authentication = getAuthentication();
        if (authentication == null) return false;

        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(ADMIN));
    }

    @Override public boolean isUser() {
        Authentication authentication = getAuthentication();
        if (authentication == null) return false;

        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(USER));
    }

    @Override public String getEmail() {
        Jwt jwt = getJwt();
        if (jwt == null) return null;

        return jwt.getClaimAsString(CLAIM);
    }
}

