package com.kaciras.blog.infra;

import com.kaciras.blog.infra.autoconfigure.BlogMybatisAutoConfiguration;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnableAspectJAutoProxy
@MapperScan
@Import({
		DataSourceAutoConfiguration.class,
		MybatisAutoConfiguration.class,
		BlogMybatisAutoConfiguration.class,
})
@ActiveProfiles("test")
@SpringBootTest
@SpringBootConfiguration
public class MybatisMapperAspectTest {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private TestMapper mapper;

	@Test
	void unwrapException(){
		assertThatThrownBy(mapper::throwException).isInstanceOf(RequestArgumentException.class);
	}

	/**
	 * Aspect 类的一些方法仅作为注解的载体，不被调用从而拉低测试覆盖率，这里给它调用一下。
	 */
	@Test
	void fixCoverage() throws Exception {
		var instance = new MybatisMapperAspect();

		var method = MybatisMapperAspect.class.getDeclaredMethod("mapper");
		method.setAccessible(true);
		method.invoke(instance);

		var method2 = MybatisMapperAspect.class.getDeclaredMethod("useProvider");
		method2.setAccessible(true);
		method2.invoke(instance);
	}

	@Mapper
	interface TestMapper {

		@SelectProvider(TestSQLProvider.class)
		void throwException();
	}

	public static class TestSQLProvider {

		public String provideSql() {
			throw new RequestArgumentException();
		}
	}
}
