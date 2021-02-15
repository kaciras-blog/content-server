package com.kaciras.blog.api;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultMatcher;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * 一些控制器返回的数据太复杂懒得一个个断言，所以就写了一个快照测试工具。
 * <p>
 * 快照保存在 src/test/resources/snapshots，属于测试代码的一部分，应当提交到版本控制系统。
 * <p>
 * 本类暂不支持并发（JUnit5 的并发测试仍处于试验阶段），且对断言方法的调用的次数和顺序必须是固定的。
 *
 * <h3>使用要求</h3>
 * 因为 JUnit 自己没有提供从外部获取当前测试名的方法，所以用了一个扩展来追踪当前测试名。
 * 请在测试类上加入：{@code @ExtendWith(Snapshots.TestContextHolder.class)}
 */
@Component
public final class SnapshotAssertion {

	private static Class<?> clazz;
	private static Method method;
	private static int index;
	private static int calls;

	private final ObjectMapper objectMapper;

	// 快照和传入对象的 JSON 字符串，使用前先调用 createOrRead()
	private String expect;
	private String actual;

	/**
	 * 因为用户可能使用自定义的 ObjectMapper，所以这里需要传入以便使配置一致。
	 *
	 * @param objectMapper 内部的转换器将复制它的配置
	 */
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
	 * 断言 MockHttpServletResponse 的响应体符合快照，用法：
	 * {@code mockMvc.perform(request).andExpect(snapshots.matchBody())}
	 *
	 * <h3>编码问题</h3>
	 * MockHttpServletResponse 默认是 ISO-8859-1，Jackson 默认是 UTF8，所以用 getContentAsByteArray()。
	 */
	public ResultMatcher matchBody() {
		return result -> assertMatch(objectMapper.readTree(result.getResponse().getContentAsByteArray()));
	}

	/**
	 * 断言 Mock 对象的方法调用参数。
	 */
	public <T> T matchArg() {
		return ArgumentMatchers.argThat(new MockitoArgMatcher<>());
	}

	/**
	 * 使用 AssertJ 来断言对象符合保存的快照。
	 * <p>
	 * 这里没有自己实现 Diff 是因为 IDE 都支持对比差异，至于控制台的话……反正我从不用。
	 */
	public void assertMatch(Object object) {
		createOrRead(object);
		assertThat(actual).isEqualTo(expect);
	}

	/**
	 * 读取快照，如果快照不存在则创建，使用 JSON 序列化将对象保存在快照文件中。
	 * 如果启动参数中设置了 {@code -DupdateSnapshot} 则强制更新快照。
	 *
	 * @param object 对象
	 */
	@SneakyThrows
	private void createOrRead(Object object) {
		actual = objectMapper.writeValueAsString(object);
		var file = getSnapshotFile();

		if (file.exists() && System.getProperty("updateSnapshot") == null) {
			expect = Files.readString(file.toPath());

			// 重新格式化，避免无效内容的影响。
			expect = objectMapper.writeValueAsString(objectMapper.readTree(expect));
		} else {
			expect = actual;
			Files.writeString(file.toPath(), actual);
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
		if (clazz == null) {
			throw new Error("必须把 TestContextHolder 注册到 JUnit 扩展");
		}
		var template = "src/test/resources/snapshots/%s/%s-%d-%d.json";
		var c = clazz.getSimpleName();
		var m = method.getName();
		return new File(String.format(template, c, m, index, calls++));
	}

	/**
	 * 用来获取当前测试名字的 JUnit5 扩展，使用了全局变量不支持并发测试。
	 * <p>
	 * 要支持并发测试的话稍加改动应该是可行的，不过调用肯定会复杂些。
	 */
	static final class TestContextHolder implements BeforeEachCallback {

		@Override
		public void beforeEach(ExtensionContext context) {
			var latestMethod = method;
			var latestClass = clazz;
			calls = 0;
			method = context.getRequiredTestMethod();
			clazz = context.getRequiredTestClass();
			index = (method == latestMethod && clazz == latestClass) ? index + 1 : 0;
		}
	}

	private final class MockitoArgMatcher<T> implements ArgumentMatcher<T> {

		// Mockito 在一次断言中可能多次调用 Matcher，但快照断言是有副作用的，所以要跳过重复的调用。
		private Boolean result;

		@Override
		public boolean matches(T argument) {
			if (result != null) {
				return result;
			}
			createOrRead(argument);
			return result = expect.equals(actual);
		}
	}
}
