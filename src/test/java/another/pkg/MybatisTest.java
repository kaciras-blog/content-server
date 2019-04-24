package another.pkg;

import org.apache.ibatis.binding.BindingException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 对 Mybatis 一些特性的验证
 */
@ActiveProfiles("test")
@SpringBootTest
final class MybatisTest {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private TestMybatisMapper mapper;

	/**
	 * 该测试验证了Mybatis不会把空值视作false
	 */
	@Test
	void testSelectNullableBoolean() {
		Assertions.assertThatThrownBy(mapper::selectNullableBool).isInstanceOf(BindingException.class);
	}
}
