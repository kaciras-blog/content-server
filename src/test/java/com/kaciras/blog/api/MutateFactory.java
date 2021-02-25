package com.kaciras.blog.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@RequiredArgsConstructor
public final class MutateFactory {

	private final ObjectMapper objectMapper;

	public <T> Builder<T> from(T base) {
		return new Builder<>(objectMapper.valueToTree(base));
	}

	@RequiredArgsConstructor
	public final class Builder<T> {

		private final ObjectNode tree;

		public Builder<T> mutate(String field, Object value) {
			tree.set(field, objectMapper.valueToTree(value));
			return this;
		}

		public String json() throws JsonProcessingException {
			return objectMapper.writeValueAsString(tree);
		}
	}
}
