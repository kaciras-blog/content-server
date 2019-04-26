package net.kaciras.blog.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.annotation.ElementType;
import java.util.Map;
import java.util.stream.Stream;

@ActiveProfiles("test")
@SpringBootTest(classes = ConfigServiceTest.TestConfig.class)
public class ConfigServiceTest {

	@Import(JacksonAutoConfiguration.class)
	@ComponentScan
	@Configuration
	static class TestConfig {

		@Bean
		public ConfigRepository configRepository() {
			var configStore = Mockito.mock(ConfigRepository.class);
			Mockito.when(configStore.loadAll())
					.thenReturn(Stream.of(new ConfigRepository.Property("test.init", "1.5")));
			return configStore;
		}
	}

	@Autowired
	private ConfigService configService;

	@Autowired
	private TestBindingBean testBean;

	@Test
	void testFire() {
		Assertions.assertThat(testBean.getIntValue()).isEqualTo(33);
		configService.set("test.int", "-123");
		Assertions.assertThat(testBean.getIntValue()).isEqualTo(-123);
	}

	/** 测试能够将枚举名转换为枚举值 */
	@Test
	void testEnumDeserialize() {
		Assertions.assertThat(testBean.getEnumValue()).isEqualTo(ElementType.FIELD);
		configService.set("test.enum", "\"PACKAGE\"");
		Assertions.assertThat(testBean.getEnumValue()).isEqualTo(ElementType.PACKAGE);
	}

	@Test
	void testInit() {
		Assertions.assertThat(testBean.getInitValue()).isEqualTo(1.5);
	}

	@Autowired
	ObjectMapper objectMapper;

	@Test
	void testA() throws IOException {
		var w = new StringWriter();
		var gen = objectMapper.getFactory().createGenerator(w);
		gen.writeStartObject();

		gen.writeFieldName("ida");
		gen.writeRawValue("123456");

		gen.writeFieldName("escape");
		gen.writeRawValue("我擦\"ee");

		gen.writeEndObject();
		gen.flush();

		var str = w.toString();
		System.out.println(str);
		var map = objectMapper.readValue(str, Map.class);
		System.out.println(map);
	}

	@Test
	void testB() throws IOException {
		var r = new StringReader("{ \"ida\": [123, 45] }");
		var p = objectMapper.getFactory().createParser(r);
		p.nextToken();
		p.nextFieldName();

		p.readValueAs(String.class);
		var next = p.nextValue();

		System.out.println(next);
	}
}
