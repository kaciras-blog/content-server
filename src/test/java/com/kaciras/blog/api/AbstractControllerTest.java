package com.kaciras.blog.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kaciras.blog.infra.principal.SecurityContextFilter;
import com.kaciras.blog.infra.principal.WebPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@EnableLoadTimeWeaving // LTW 简直毒瘤啊，各种毛病烦死人。
@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SnapshotAssertion.ContextHolder.class)
public abstract class AbstractControllerTest {

	public static final WebPrincipal ADMIN = new WebPrincipal(WebPrincipal.ADMIN_ID);
	public static final WebPrincipal ANONYMOUS = new WebPrincipal(WebPrincipal.ANONYMOUS_ID);
	public static final WebPrincipal LOGINED = new WebPrincipal(666);

	protected MockMvc mockMvc;

	@Autowired
	protected WebApplicationContext wac;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected SnapshotAssertion snapshot;

	// 本类里的一些方法设置了 final 为了防止子类不小心给覆盖了，虽然我不觉得会出这种失误。
	@BeforeEach
	final void setup() {
		var requestTemplate = get("/")
				.contentType(MediaType.APPLICATION_JSON)
				.principal(ANONYMOUS)
				.characterEncoding("UTF-8");

		mockMvc = MockMvcBuilders.webAppContextSetup(wac)
				.defaultRequest(requestTemplate)
				.addFilter(new SecurityContextFilter())
				.alwaysDo(r -> r.getResponse().setCharacterEncoding("UTF-8"))
				.build();
	}

	/**
	 * 把对象序列化为 JSON 字符串，因为比较长所以提取单独一个方法。
	 *
	 * @param value 对象
	 * @return JSON 字符串
	 */
	protected final String toJson(Object value) throws IOException {
		return objectMapper.writeValueAsString(value);
	}

	/**
	 * 修改一个对象指定的字段，返回新的对象。该方法可以用于任何字段，即便是 final 也行。
	 *
	 * <h2>使用场景</h2>
	 * 测试对象各个字段的边界情况，先创建一个正常对象，然后修改字段，在字段多的情况下能省点代码。
	 *
	 * @param base 原始对象
	 * @param field 字段名
	 * @param value 字段新值
	 * @param <T> 对象类型
	 * @return 修改后的新对象
	 */
	@SuppressWarnings("unchecked")
	protected final <T> T mutate(T base, String field, Object value) throws IOException {
		var tree = (ObjectNode)objectMapper.valueToTree(base);
		tree.set(field, objectMapper.valueToTree(value));
		return (T) objectMapper.treeToValue(tree, base.getClass());
	}
}
