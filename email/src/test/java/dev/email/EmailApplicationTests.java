package dev.email;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires DB, RabbitMQ, SMTP and env vars. Prefer unit tests.")
class EmailApplicationTests {

	@Test
	void contextLoads() {
	}

}
