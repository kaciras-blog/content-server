package com.kaciras.blog.api.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.SessionRepository;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"unchecked", "rawtypes"})
@ActiveProfiles("test")
@SpringBootTest
final class HttpSessionTableTest {

	@Autowired
	private SessionRepository repository;

	@Autowired
	private HttpSessionTable table;

	@Autowired
	private RedisTemplate<String, String> redis;

	@BeforeEach
	void flushDb() {
		redis.getRequiredConnectionFactory().getConnection().serverCommands().flushDb();
	}

	@Test
	void clearAllEmpty() {
		table.clearAll(1);
		table.cleanAccountRecords();
	}

	@Test
	void clearAll() {
		var session = repository.createSession();
		session.setLastAccessedTime(Instant.now());
		session.setMaxInactiveInterval(Duration.ofDays(1));
		repository.save(session);
		table.add(1, session.getId());

		table.clearAll(1);

		session = repository.findById(session.getId());
		assertThat(session).isNull();
	}

	@Test
	void cleanAccountRecords() {
		var session = repository.createSession();
		session.setLastAccessedTime(Instant.now());
		session.setMaxInactiveInterval(Duration.ofDays(1));
		repository.save(session);
		table.add(1, session.getId());

		repository.deleteById(session.getId());
		table.cleanAccountRecords();

		assertThat(redis.keys("*")).hasSize(0);
	}
}
