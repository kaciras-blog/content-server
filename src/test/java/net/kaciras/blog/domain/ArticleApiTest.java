package net.kaciras.blog.domain;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;

public class ArticleApiTest extends AbstractSpringTest {

	@Test
	void testGetList() throws Exception {
		restTemplate.getForEntity("/articles", JsonNode.class);
	}

	@Test
	void testChangeCategory() throws Exception {
		var content = objectMapper.createObjectNode()
				.put("category", 123)
				.put("deletion", false)
				.toString();

//		mockMvc.perform(patch("/articles/1").content(content)).andExpect(status().isNoContent());
	}

	private void matchArticleMetadataList(MvcResult result) throws IOException {
		objectMapper.readTree(result.getResponse().getContentAsByteArray())
				.elements()
				.forEachRemaining(this::checkMetadata);
	}

	private void matchArticleMetadata(MvcResult result) throws IOException {
		checkMetadata(objectMapper.readTree(result.getResponse().getContentAsByteArray()));
	}

	private void checkMetadata(JsonNode node) {
		node.get("id").asInt();
		node.get("title").asText();
	}
}
