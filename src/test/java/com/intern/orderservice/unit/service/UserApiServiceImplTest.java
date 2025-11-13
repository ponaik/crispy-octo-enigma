package com.intern.orderservice.unit.service;

import com.intern.orderservice.dto.response.UserResponse;
import com.intern.orderservice.service.impl.UserApiServiceImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)   // modern way to enable Mockito in JUnit 5
class UserApiServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    private final String USERS_SEARCH_ENDPOINT = "/users/search?email=";
    private final String USERS_ENDPOINT = "/users/";
    private final String USERSERVICE_URL = "http://userservice";

    private UserApiServiceImpl service() {
        return new UserApiServiceImpl(restTemplate, USERS_SEARCH_ENDPOINT, USERS_ENDPOINT, USERSERVICE_URL);
    }

    @Test
    void givenExistingUserId_whenGetUserById_thenReturnsUserResponse() {
        // Given
        Long userId = 42L;
        UserResponse expected = new UserResponse(userId, "John", "Doe",
                LocalDate.of(1990, 1, 1), "john.doe@example.com");
        String url = USERSERVICE_URL + USERS_ENDPOINT + userId;
        ResponseEntity<UserResponse> entity = new ResponseEntity<>(expected, HttpStatus.OK);
        when(restTemplate.getForEntity(url, UserResponse.class)).thenReturn(entity);

        // When
        UserResponse actual = service().getUserById(userId);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(expected.id());
        assertThat(actual.name()).isEqualTo(expected.name());
        assertThat(actual.surname()).isEqualTo(expected.surname());
        assertThat(actual.birthDate()).isEqualTo(expected.birthDate());
        assertThat(actual.email()).isEqualTo(expected.email());
    }

    @Test
    void givenExistingEmail_whenGetUserByEmail_thenReturnsUserResponse() {
        // Given
        String email = "alice@example.com";
        UserResponse expected = new UserResponse(7L, "Alice", "Smith",
                LocalDate.of(1985, 6, 15), email);
        String url = USERSERVICE_URL + USERS_SEARCH_ENDPOINT + email;
        ResponseEntity<UserResponse> entity = new ResponseEntity<>(expected, HttpStatus.OK);
        when(restTemplate.getForEntity(url, UserResponse.class)).thenReturn(entity);

        // When
        UserResponse actual = service().getUserByEmail(email);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(expected.id());
        assertThat(actual.email()).isEqualTo(expected.email());
        assertThat(actual.name()).isEqualTo(expected.name());
    }

    @Test
    void givenHttp404Error_whenGetUserByIdFallback_thenReturnsNotFoundPlaceholder() {
        // Given
        Long userId = 100L;
        HttpClientErrorException notFound =
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);

        // When
        UserResponse fallback = service().getUserByIdFallback(userId, notFound);

        // Then
        assertThat(fallback).isNotNull();
        assertThat(fallback.id()).isNull();
        assertThat(fallback.name()).isEqualTo(HttpStatus.NOT_FOUND.toString());
        assertThat(fallback.surname()).isNull();
        assertThat(fallback.birthDate()).isNull();
        assertThat(fallback.email()).isNull();
    }

    @Test
    void givenHttp500Error_whenGetUserByIdFallback_thenRethrowsException() {
        // Given
        Long userId = 200L;
        HttpClientErrorException serverError =
                HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, null, null);

        // When / Then
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> service().getUserByIdFallback(userId, serverError));
        assertThat(thrown).isSameAs(serverError);
    }
}
