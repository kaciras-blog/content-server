package com.kaciras.blog.api;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Cleanup;
import org.aspectj.util.FileUtil;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultMatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * 搜了一圈也没看到快照测试相关的库，只能自己写一个。
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@Component
public final class Snapshots {

	private final ObjectMapper objectMapper;

	public Snapshots(ObjectMapper objectMapper) {
		var indenter = new DefaultIndenter().withIndent("\t");

		var printer = new DefaultPrettyPrinter()
				.withArrayIndenter(indenter)
				.withObjectIndenter(indenter);

		this.objectMapper = objectMapper.copy()
				.enable(SerializationFeature.INDENT_OUTPUT)
				.setDefaultPrettyPrinter(printer)
				.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
				.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
	}

	public ResultMatcher expectBodyToMatchSnapshot() {
		return result -> assertMatch(objectMapper.readTree(result.getResponse().getContentAsByteArray()));
	}

	public void assertMatch(Object object) throws Exception {
		var actual = objectMapper.writeValueAsString(object);
		var file = getSnapshotFile();

		if (file.exists()) {
			assertThat(actual).isEqualTo(FileUtil.readAsString(file));
		} else {
			file.getParentFile().mkdirs();
			@Cleanup var stream = new FileOutputStream(actual);
			stream.write(actual.getBytes(StandardCharsets.UTF_8));
		}
	}

	private File getSnapshotFile() {
		var template = "src/test/resources/snapshots/%s/%s.json";

		var caller = Stream.of(Thread.currentThread().getStackTrace())
				.skip(1) // 跳过 getStackTrace() 自身
				.filter(e -> !e.getClassName().equals(Snapshots.class.getName()))
				.findFirst()
				.orElseThrow();

		var parts = caller.getClassName().split("\\.");
		var testGroup = parts[parts.length - 1];
		var testName = caller.getMethodName();

		return new File(String.format(template, testGroup, testName));
	}
}
