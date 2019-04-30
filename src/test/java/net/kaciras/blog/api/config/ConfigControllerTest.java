package net.kaciras.blog.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.kaciras.blog.infrastructure.autoconfig.KxWebUtilsAutoConfiguration;
import net.kaciras.blog.infrastructure.principal.AuthorizeAspect;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import net.kaciras.blog.infrastructure.principal.WebPrincipal;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureWebMvc
@AutoConfigureMockMvc
@SpringBootTest(classes = ConfigControllerTest.NestedConfig.class)
public class ConfigControllerTest {

	@EnableAspectJAutoProxy
	@Import({KxWebUtilsAutoConfiguration.class})
	@Configuration
	static class NestedConfig {

		@Bean
		public AuthorizeAspect principalAspect() {
			return new AuthorizeAspect();
		}

		@Bean
		public ConfigController controller(ConfigService configService, ObjectMapper objectMapper) {
			return new ConfigController(configService, objectMapper);
		}
	}

	@Autowired
	private MockMvc webClient;

	@MockBean
	private ConfigService configService;

	@BeforeEach
	void setUp() {
		SecurityContext.setPrincipal(new WebPrincipal(WebPrincipal.ADMIN_ID));
	}

	@Test
	void testGet() throws Exception {
		Mockito.when(configService.get("test.config")).thenReturn(new TestBindingConfig());

		webClient.perform(get("/config/test.config"))
				.andExpect(status().is(200))
				.andExpect(jsonPath("$.intValue").value(33))
				.andExpect(jsonPath("$.enumValue").value("FIELD"));
	}

	@Test
	void testGetNonExistent() throws Exception {
		Mockito.when(configService.get("non.existent")).thenReturn(null);
		webClient.perform(get("/config/non.existent")).andExpect(status().is(404));
	}

	@Test
	void testPatch() throws Exception {
		Mockito.when(configService.get("test.config")).thenReturn(new TestBindingConfig());

		var content = "{ \"enumValue\": \"METHOD\" }";
		webClient.perform(patch("/config/test.config").content(content))
				.andExpect(status().is(204));

		var captor = ArgumentCaptor.forClass(TestBindingConfig.class);
		Mockito.verify(configService).set(eq("test.config"), captor.capture());
		var newConfig = captor.getValue();

		Assertions.assertThat(newConfig.getEnumValue()).isEqualTo(ElementType.METHOD);
		Assertions.assertThat(newConfig.getIntValue()).isEqualTo(33);
	}

	@Test
	void testPatchNonExistent() throws Exception {
		Mockito.when(configService.get("non.existent")).thenReturn(null);
		webClient.perform(patch("/config/non.existent")).andExpect(status().is(404));
	}

	@Test
	void testPatchPermission() throws Exception {
		SecurityContext.setPrincipal(new WebPrincipal(WebPrincipal.ANONYMOUS_ID));
		webClient.perform(patch("/config/test.config")).andExpect(status().is(403));
	}
}
