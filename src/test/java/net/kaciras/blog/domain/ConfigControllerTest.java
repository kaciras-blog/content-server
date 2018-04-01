package net.kaciras.blog.domain;

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
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@Test
	void testAnnontationBinding() throws Exception {
		BindingObject bindingObject = wac.getBean(BindingObject.class);
		Assertions.assertThat(bindingObject.value0).isEqualTo(123456);
		Assertions.assertThat(bindingObject.enumValue).isEqualTo(ElementType.FIELD);

		MvcResult result = mockMvc.perform(MockMvcRequestBuilders
				.put("/configs/test.config.binding.v0")
				.requestAttr("value", 654321)).andReturn();
		Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(204);
		Assertions.assertThat(bindingObject.value0).isEqualTo(654321);

		result = mockMvc.perform(MockMvcRequestBuilders
				.put("/configs/test.config.binding.enum")
				.requestAttr("value", "METHOD")).andReturn();
		Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(204);
		Assertions.assertThat(bindingObject.enumValue).isEqualTo(ElementType.METHOD);

		result = mockMvc.perform(MockMvcRequestBuilders
				.put("/configs/test.config.binding.enum")
				.requestAttr("value", 123456)).andReturn();
		Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(400);
		Assertions.assertThat(bindingObject.enumValue).isEqualTo(ElementType.METHOD);
	}

	@Component
	public static class BindingObject {

		@ConfigBind("test.config.binding.v0")
		private int value0;

		private ElementType enumValue;

		@ConfigBind("test.config.binding.enum")
		public void setEnumValue(ElementType enumValue) {
			this.enumValue = enumValue;
		}
	}
}
