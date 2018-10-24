package net.kaciras.blog.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.kaciras.blog.api.ServiceApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = ServiceApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public abstract class AbstractSpringTest {

	protected TestRestTemplate restTemplate = new TestRestTemplate();

	@Autowired
	protected ObjectMapper objectMapper;


	@BeforeEach
	void setup() {
	}
}
