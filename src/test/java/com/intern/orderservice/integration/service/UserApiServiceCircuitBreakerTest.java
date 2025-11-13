package com.intern.orderservice.integration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.orderservice.dto.response.UserResponse;
import com.intern.orderservice.service.UserApiService;
import com.intern.orderservice.service.impl.UserApiServiceImpl;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {UserApiServiceImpl.class, UserApiServiceCircuitBreakerTest.TestConfig.class})
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        LiquibaseAutoConfiguration.class
})
@AutoConfigureWireMock(port = 9099)
@ActiveProfiles("test")
@Tag("integration")
class UserApiServiceCircuitBreakerTest {

    public static final int THRESHOLD = 10;
    public static final int RECOVERY_TIME = 1;

    @TestConfiguration
    static class TestConfig {
        @Bean
        RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void resetBreaker() {
        circuitBreakerRegistry.circuitBreaker("UserService").reset();
    }

    @Autowired
    private UserApiService userApiService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse fakeUser;

    @BeforeEach
    void setup() throws Exception {
        fakeUser = new UserResponse(
                1L,
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "john.doe@example.com"
        );

        // Stub successful response
        stubFor(get(urlEqualTo("/users/" + fakeUser.id()))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(fakeUser))
                        .withStatus(200)));
    }

    @Test
    void testSuccessfulCallDoesNotTriggerFallback() {
        UserResponse response = userApiService.getUserById(fakeUser.id());
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(fakeUser.email());
    }

    @Test
    void testNotFoundTriggersFallback() {
        // Stub 404 response
        stubFor(get(urlEqualTo("/users/999"))
                .willReturn(aResponse().withStatus(404)));

        UserResponse response = userApiService.getUserById(999L);

        // Fallback should return placeholder
        assertThat(response.id()).isNull();
        assertThat(response.name()).isEqualTo(HttpStatus.NOT_FOUND.toString());
    }

    @Test
    void testCircuitBreakerOpensAfterFailures() {
        // Stub repeated 500 errors
        stubFor(get(urlEqualTo("/users/500"))
                .willReturn(aResponse().withStatus(500)));

        // Cause multiple failures to exceed failureRateThreshold
        for (int i = 0; i < THRESHOLD + 1; i++) {
            try {
                userApiService.getUserById(500L);
            } catch (Exception ignored) {
            }
        }

        // Now circuit breaker should be OPEN → calls short-circuited
        assertThatThrownBy(() -> userApiService.getUserById(500L))
                .isInstanceOf(CallNotPermittedException.class);
    }

    @Test
    void testCircuitBreakerHalfOpenAndRecovery() throws Exception {
        // Stub 500 error
        stubFor(get(urlEqualTo("/users/501"))
                .willReturn(aResponse().withStatus(500)));

        // Cause failures
        for (int i = 0; i < THRESHOLD + 1; i++) {
            try {
                userApiService.getUserById(501L);
            } catch (Exception ignored) {
            }
        }

        Thread.sleep(RECOVERY_TIME + 1000);

        // Stub recovery response
        stubFor(get(urlEqualTo("/users/501"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(fakeUser))
                        .withStatus(200)));

        // Circuit breaker should allow calls again
        UserResponse response = userApiService.getUserById(501L);
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(fakeUser.email());
    }
}
