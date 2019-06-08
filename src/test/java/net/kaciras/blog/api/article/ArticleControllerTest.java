package net.kaciras.blog.api.article;

import com.fasterxml.jackson.databind.JsonNode;
import net.kaciras.blog.api.AbstractSpringTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ArticleControllerTest extends AbstractSpringTest {

	private JsonNode queryArticle(int id) throws Exception {
		return objectMapper.readTree(mockMvc.perform(get("/articles/" + id))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString());
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

	@Test
	void testGetList() throws Exception {
		mockMvc.perform(get("/articles"))
				.andExpect(status().isOk())
				.andExpect(this::matchArticleMetadataList);
	}

	@Test
	void testChangeCategory() throws Exception {
		Assertions.assertThat(queryArticle(1).get("category").asInt()).isEqualTo(6);

		var content = objectMapper.createObjectNode().put("category", 17).toString();

		// 测试权限
		mockMvc.perform(patch("/articles/1").content(content))
				.andExpect(status().isForbidden());

		mockMvc.perform(patch("/articles/1").content(content).principal(ADMIN))
				.andExpect(status().isNoContent());

		Assertions.assertThat(queryArticle(1).get("category").asInt()).isEqualTo(17);
	}

	@Test
	void testChangeDeletion() throws Exception {
		Assertions.assertThat(queryArticle(1).get("deleted").asBoolean()).isFalse();

		var content = objectMapper.createObjectNode().put("deletion", false).toString();
		mockMvc.perform(patch("/articles/1").content(content))
				.andExpect(status().isForbidden());
		mockMvc.perform(patch("/articles/1").content(content).principal(ADMIN))
				.andExpect(status().isConflict());

		// 将文章设为删除
		content = objectMapper.createObjectNode().put("deletion", true).toString();
		mockMvc.perform(patch("/articles/1").content(content).principal(ADMIN))
				.andExpect(status().isNoContent());

		Assertions.assertThat(queryArticle(1).get("deleted").asBoolean()).isTrue();
	}
}
