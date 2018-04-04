package net.kaciras.blog.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.annotation.ElementType;

public class ConfigControllerTest extends AbstractSpringTest {

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private ObjectMapper objectMapper;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@Test
	void testAnnontationBinding() throws Exception {
		BindingObject bindingObject = wac.getBean(BindingObject.class);
		Assertions.assertThat(bindingObject.value0).isEqualTo(123456);
		Assertions.assertThat(bindingObject.enumValue).isEqualTo(TestEnumConfig.FIRST);

		MvcResult result = mockMvc.perform(MockMvcRequestBuilders
				.put("/configs/test.config.binding.v0")
				.param("value", "654321")).andReturn();
		Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(204);
		Assertions.assertThat(bindingObject.value0).isEqualTo(654321);

		result = mockMvc.perform(MockMvcRequestBuilders
				.put("/configs/test.config.binding.enum")
				.param("value", "SECOND")).andReturn();
		Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(204);
		Assertions.assertThat(bindingObject.enumValue).isEqualTo(TestEnumConfig.SECOND);

		result = mockMvc.perform(MockMvcRequestBuilders
				.put("/configs/test.config.binding.enum")
				.param("value", "123456")).andReturn();
		Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(400);
		Assertions.assertThat(bindingObject.enumValue).isEqualTo(TestEnumConfig.SECOND);
	}

	@Test
	void testGetConfigs() throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/configs")).andReturn();
		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		Assertions.assertThat(json.get("test")).size().isEqualTo(2);
	}

	@Component
	public static class BindingObject {

		@ConfigBind("test.config.binding.v0")
		private int value0;

		private TestEnumConfig enumValue;

		@ConfigBind("test.config.binding.enum")
		public void setEnumValue(TestEnumConfig enumValue) {
			this.enumValue = enumValue;
		}
	}

	enum TestEnumConfig {

		@EnumConfigItem("Description of first item")
		FIRST,

		@EnumConfigItem("Description of second item")
		SECOND,

		LAST,
	}
}
