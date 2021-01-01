package com.kaciras.blog.api;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Cleanup;
import org.aspectj.util.FileUtil;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultMatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * 快照测试工具，我搜了一圈也没看到快照测试相关的库，只能自己写一个。
 * <p>
 * 本工具不支持并发测试，且对断言方法的调用的次数和顺序必须是固定的。
 * <p>
 * 因为 JUnit 自己没有提供外部获取当前测试的方法，故需要注册一个扩展来追踪当前测试名.
 * 请在测试类上加入：
 * {@code @ExtendWith(Snapshots.TestContextHolder.class)}
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@Component
public final class SnapshotAssertion {

	private static String testClass;
	private static String testName;
	private static int index;

	private final ObjectMapper objectMapper;

	public SnapshotAssertion(ObjectMapper objectMapper) {
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

	/**
	 * 断言 MockHttpServletResponse 的响应体符合快照。
	 * <p>
	 * {@code mockMvc.perform(request).andExpect(snapshots.bodyToMatch())}
	 */
	public ResultMatcher matchBody() {
		return result -> assertMatch(objectMapper.readTree(result.getResponse().getContentAsByteArray()));
	}

	/**
	 * 断言一个对象符合快照，使用 JSON 序列化将对象保存在快照文件中。
	 */
	public void assertMatch(Object object) throws Exception {
		var actual = objectMapper.writeValueAsString(object);
		var file = getSnapshotFile();

		if (file.exists()) {
			assertThat(actual).isEqualTo(FileUtil.readAsString(file));
		} else {
			file.getParentFile().mkdirs();
			@Cleanup var stream = new FileOutputStream(file);
			stream.write(actual.getBytes(StandardCharsets.UTF_8));
		}
	}

	/**
	 * 获取当前断言对应的快照文件，每个测试中每一次调用都会返回不同的结果。
	 *
	 * <h2>测试名的获取</h2>
	 * 比起通过调用栈查找测试方法，BeforeEachCallback.beforeEach 更方便，而且不受嵌套调用的影响。
	 *
	 * @return 当前断言对应的快照文件
	 */
	private File getSnapshotFile() {
		if (testClass == null) {
			throw new Error("必须把 TestContextHolder 注册到 JUnit 扩展才能使用快照");
		}
		var template = "src/test/resources/snapshots/%s/%s-%d.json";
		return new File(String.format(template, testClass, testName, index++));
	}

	static final class TestContextHolder implements BeforeEachCallback {

		@Override
		public void beforeEach(ExtensionContext context) {
			index = 0;
			testClass = context.getRequiredTestClass().getSimpleName();
			testName = context.getRequiredTestMethod().getName();
		}
	}
}
