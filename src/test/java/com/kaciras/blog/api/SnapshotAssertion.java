package com.kaciras.blog.api;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultMatcher;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 一些控制器返回的数据太复杂懒得一个个断言，所以就写了一个快照测试工具。
 * <p>
 * 快照保存在 src/test/resources/snapshots，属于测试代码的一部分，应当提交到版本控制系统。
 * 如果启动参数中设置了 {@code -DupdateSnapshot} 则强制更新快照。
 *
 * <h2>暂不支持并发测试</h2>
 * 因为使用了全局字段所以不支持并发，JUnit5 的并发测试仍处于试验阶段，暂未研究。
 *
 * <h2>使用要求</h2>
 * 因为 JUnit 自己没有提供从外部获取当前测试名的方法，所以用了一个扩展来追踪当前测试名。
 * 请在测试类上加入：{@code @ExtendWith(SnapshotAssertion.ContextHolder.class)}
 */
@Component
public final class SnapshotAssertion {

	/** 当前测试的方法 */
	private static Method method;

	/** 当前测试运行到第几个参数，不是参数测试则为0 */
	private static int index;

	/** 在测试中第几次执行断言 */
	private static int calls;

	private final ObjectMapper objectMapper;
	private final boolean forceUpdate;

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

		this.forceUpdate = System.getProperty("updateSnapshot") != null;
	}

	/**
	 * 断言 MockHttpServletResponse 的响应体符合快照，用法：
	 * {@code mockMvc.perform(request).andExpect(snapshots.matchBody());}
	 *
	 * <h3>编码问题</h3>
	 * MockHttpServletResponse 默认是 ISO-8859-1，Jackson 默认是 UTF8，所以用 getContentAsByteArray()。
	 */
	public ResultMatcher matchBody() {
		return result -> assertMatch(objectMapper.readTree(result.getResponse().getContentAsByteArray()));
	}

	/**
	 * 断言 Mockito 的 Mock 对象的方法调用参数符合快照，用法：
	 * {@code verify(mock).someMethod(snapshots.matchArg());}
	 */
	public <T> T matchArg() {
		return ArgumentMatchers.argThat(new MockitoArgMatcher<>());
	}

	/**
	 * 使用 AssertJ 来断言对象与保存的快照一致。
	 * <p>
	 * 这里没有自己实现 Diff 是因为 IDE 都支持对比差异，至于控制台的话……反正我从不用。
	 */
	public void assertMatch(Object object) {
		createOrLoad(object).assertEquals();
	}

	/**
	 * 读取快照，如果快照不存在则创建，使用 JSON 序列化将对象保存在快照文件中。
	 * 序列化后的对象和快照分别赋值给 {@code actual} 和 {@code expect} 字段。
	 *
	 * @param object 需要保存到快照的对象
	 */
	@SneakyThrows
	private Matching createOrLoad(Object object) {
		var actual = objectMapper.writeValueAsString(object);
		var expect = actual;
		var path = getSnapshotPath();

		if (!Files.exists(path) || forceUpdate) {
			Files.createDirectories(path.getParent());
			Files.writeString(path, actual);
		} else {
			// 重新格式化，避免手动美化快照文件造成的影响。
			var tree = objectMapper.readTree(Files.newInputStream(path));
			expect = objectMapper.writeValueAsString(tree);
		}

		return new Matching(expect, actual);
	}

	/**
	 * 获取当前断言对应的快照文件路径。
	 *
	 * <h2>测试名的获取</h2>
	 * 比起通过调用栈查找测试方法，BeforeEachCallback 更方便，而且不受嵌套调用的影响。
	 * 这里不区分参数化测试，普通测试方法的参数索引为0.
	 *
	 * @return 快照文件的路径
	 */
	private Path getSnapshotPath() {
		if (method == null) {
			throw new Error("必须把 ContextHolder 注册到 JUnit 扩展");
		}
		var template = "src/test/resources/snapshots/%s/%s-%d-%d.json";
		var c = method.getDeclaringClass().getSimpleName();
		var m = method.getName();
		return Paths.get(String.format(template, c, m, index, calls++));
	}

	/**
	 * 用来获取当前测试名字的 JUnit5 扩展，使用了全局变量不支持并发测试。
	 * <p>
	 * 要支持并发测试的话稍加改动应该是可行的，不过调用肯定会复杂些。
	 */
	public static final class ContextHolder implements BeforeEachCallback {

		@Override
		public void beforeEach(ExtensionContext context) {
			var latestMethod = method;
			method = context.getRequiredTestMethod();
			calls = 0;
			index = method == latestMethod ? index + 1 : 0;
		}
	}

	/**
	 * 一次快照匹配过程，包含了从对象序列化来的和从快照读取的 JSON 字符串。
	 */
	@RequiredArgsConstructor
	private static final class Matching {

		private final String expect;
		private final String actual;

		public boolean isEquals() {
			return actual.equals(expect);
		}

		public void assertEquals() {
			assertThat(actual).isEqualTo(expect);
		}
	}

	private final class MockitoArgMatcher<T> implements ArgumentMatcher<T> {

		// 测试中发现一次断言可能多次调用 matches，但创建快照有副作用，所以要跳过重复的调用。
		private Matching matching;

		@Override
		public boolean matches(T argument) {
			if (matching == null) {
				matching = createOrLoad(argument);
			}
			return matching.isEquals();
		}
	}
}
