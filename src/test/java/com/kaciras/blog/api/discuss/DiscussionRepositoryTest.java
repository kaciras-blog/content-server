package com.kaciras.blog.api.discuss;

import com.kaciras.blog.infra.exception.RequestArgumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
class DiscussionRepositoryTest {

	@Autowired
	private DiscussionRepository repository;

	private static Discussion newValue() {
		var value = new Discussion();
		value.setContent("test content");
		value.setAddress(InetAddress.getLoopbackAddress());
		value.setState(DiscussionState.Visible);
		return value;
	}

	@Test
	void add() {
		var value = newValue();
		repository.add(value);
		assertThat(value.getId()).isEqualTo(1);
	}

	private static Stream<Arguments> InvalidDiscussions() {
		var noContent = newValue();
		noContent.setContent(null);

		var noAddress = newValue();
		noAddress.setAddress(null);

		var noState = newValue();
		noState.setState(null);

		return Stream.of(Arguments.of(noContent), Arguments.of(noAddress), Arguments.of(noState));
	}

	@MethodSource("InvalidDiscussions")
	@ParameterizedTest
	void addInvalid(Discussion value) {
		assertThatThrownBy(() -> repository.add(value)).isInstanceOf(Exception.class);
	}

	@Test
	void findWithInvalidSort() {
		var query = new DiscussionQuery();
		query.setPageable(PageRequest.of(0, 1, Sort.by("content")));

		assertThatThrownBy(() -> repository.findAll(query)).isInstanceOf(RequestArgumentException.class);
	}

	@Test
	void findAll() {
		repository.findAll(new DiscussionQuery());
	}

	@Test
	void updateAll() {

	}
}
