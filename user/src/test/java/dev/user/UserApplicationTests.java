package dev.user;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires DB, RabbitMQ and env vars (DB_URL, SECRET, MQ_*). Prefer unit tests.")
class UserApplicationTests {

	@Test
	void contextLoads() {
	}

}
