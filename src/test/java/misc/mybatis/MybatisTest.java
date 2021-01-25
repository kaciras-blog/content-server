/*
 * 对 Mybatis 一些特性的验证
 */
package misc.mybatis;

import com.kaciras.blog.infra.autoconfigure.KxCodecAutoConfiguration;
import org.apache.ibatis.binding.BindingException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// DataSourceAutoConfiguration 内置的扫描器值扫描标记为 AutoConfigurationPackages(@EnableAutoConfiguration) 的包，
// 如果待扫描的Mapper不在这些包里就需要手动添加 @MapperScan("xxx") 来扫描
@MapperScan("misc.mybatis")
@ImportAutoConfiguration({
		DataSourceAutoConfiguration.class,
		MybatisAutoConfiguration.class,
		KxCodecAutoConfiguration.class})
@ActiveProfiles("test")
@SpringBootTest
@SpringBootConfiguration
class MybatisTest {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private TestMybatisMapper mapper;

	/** 验证Mybatis不会把空值视作false */
	@Test
	void testSelectNullableBoolean() {
		Assertions.assertThatThrownBy(mapper::selectNullableBool).isInstanceOf(BindingException.class);
	}

	/** Mybatis 不支持 OptionalInt */
	@Test
	void testOptionalPrimitives() {
		var optInt = mapper.selectOptionalInt();
		Assertions.assertThat(optInt).isNull();
	}
}
