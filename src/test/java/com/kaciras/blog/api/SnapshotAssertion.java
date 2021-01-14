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
 * 控制器返回的数据太复杂懒得一个个断言，所以就需要一个快照测试工具，
 * 我搜了一圈也没看到好用的快照测试库，只能自己写一个。
 * <p>
 * 快照文件保存在 src/test/resources/snapshots 目录下，属于源码的一部分，应当提交到版本控制系统。
 * <p>
 * 本工具不支持并发测试，且对断言方法的调用的次数和顺序必须是固定的。
 *
 * <h3>使用要求</h3>
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
				.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
				.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
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
	 * 如果启动参数中设置了 -DupdateSnapshot 则强制更新快照。
	 * <p>
	 * 这里直接使用 AssertJ 的断言而不是自己实现 Diff，因为 IDE 都支持对比差异，
	 * 至于控制台的话……反正我从不用。
	 */
	public void assertMatch(Object object) throws Exception {
		var actual = objectMapper.writeValueAsString(object);
		var file = getSnapshotFile();

		if (file.exists() && System.getProperty("updateSnapshot") == null) {
			var expect = FileUtil.readAsString(file);

			// 重新格式化，避免快照文件格式的影响。
			expect = objectMapper.writeValueAsString(objectMapper.readTree(expect));
			assertThat(actual).isEqualTo(expect);
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
			throw new Error("必须把 TestContextHolder 注册到 JUnit 扩展");
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
