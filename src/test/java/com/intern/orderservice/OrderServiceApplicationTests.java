package com.intern.orderservice;

import com.intern.orderservice.integration.CustomPostgreSQLContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceApplicationTests extends CustomPostgreSQLContainer {

	@Test
	void contextLoads() {
	}

}
