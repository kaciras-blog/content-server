package net.kaciras.blog.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.kaciras.blog.infrastructure.autoconfigure.KxWebUtilsAutoConfiguration;
import net.kaciras.blog.infrastructure.principal.AuthorizeAspect;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import net.kaciras.blog.infrastructure.principal.WebPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.annotation.ElementType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureWebMvc
@AutoConfigureMockMvc
@SpringBootTest(classes = ConfigControllerTest.NestedConfig.class)
final class ConfigControllerTest {

	@EnableAspectJAutoProxy
	@Import({KxWebUtilsAutoConfiguration.class})
	@Configuration
	static class NestedConfig {

		@Bean
		public AuthorizeAspect principalAspect() {
			return new AuthorizeAspect();
		}

		@Bean
		public ConfigController controller(ConfigBindingManager configBindingManager, ObjectMapper objectMapper) {
			return new ConfigController(configBindingManager, objectMapper);
		}
	}

	@MockBean
	private ConfigBindingManager configBindingManager;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MockMvc webClient;

	@BeforeEach
	void setUp() {
		SecurityContext.setPrincipal(new WebPrincipal(WebPrincipal.ADMIN_ID));
	}

	@Test
	void testGet() throws Exception {
		when(configBindingManager.get("test.config")).thenReturn(new TestBindingConfig());

		webClient.perform(get("/config/test.config"))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.intValue").value(33))
				.andExpect(jsonPath("$.enumValue").value("FIELD"));
	}

	@Test
	void testGetNonExistent() throws Exception {
		when(configBindingManager.get("non.existent")).thenReturn(null);
		webClient.perform(get("/config/non.existent")).andExpect(status().is(404));
	}

	@Test
	void testPatch() throws Exception {
		when(configBindingManager.get("test.config")).thenReturn(new TestBindingConfig());

		var content = "{ \"enumValue\": \"METHOD\" }";
		var body = webClient.perform(patch("/config/test.config").content(content))
				.andExpect(status().is(200))
				.andReturn().getResponse().getContentAsString();

		// 这里非得用 eq() 不可
		var captor = ArgumentCaptor.forClass(TestBindingConfig.class);
		verify(configBindingManager).set(eq("test.config"), captor.capture());
		var newConfig = captor.getValue();

		assertThat(newConfig.getEnumValue()).isEqualTo(ElementType.METHOD);
		assertThat(newConfig.getIntValue()).isEqualTo(33);

		var updated = objectMapper.readValue(body, TestBindingConfig.class);
		assertThat(updated.getEnumValue()).isEqualTo(ElementType.METHOD);
	}

	@Test
	void testPatchNonExistent() throws Exception {
		when(configBindingManager.get("non.existent")).thenReturn(null);
		webClient.perform(patch("/config/non.existent")).andExpect(status().is(404));
	}

	@Test
	void testPatchPermission() throws Exception {
		SecurityContext.setPrincipal(new WebPrincipal(WebPrincipal.ANONYMOUS_ID));
		webClient.perform(patch("/config/test.config")).andExpect(status().is(403));
	}
}
