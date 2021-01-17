package com.kaciras.blog.infra.principal;

import com.kaciras.blog.infra.exception.DataTooBigException;
import com.kaciras.blog.infra.exception.PermissionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = AuthorizeAspectTest.EmbeddedConfiguration.class)
class AuthorizeAspectTest {

	public static class MethodAopBean {

		@RequirePermission
		void requireAuth() {}
	}

	@RequirePermission
	public static class ClassAopBean {

		void requireAuth() {}

		@RequirePermission(error = DataTooBigException.class)
		void customException() {}
	}

	@EnableAspectJAutoProxy(proxyTargetClass = true)
	@Configuration(proxyBeanMethods = false)
	static class EmbeddedConfiguration {

		@Bean
		public MethodAopBean methodAopBean() {
			return new MethodAopBean();
		}

		@Bean
		public ClassAopBean classAopBean() {
			return new ClassAopBean();
		}

		@Bean
		public AuthorizeAspect principalAspect() {
			return new AuthorizeAspect();
		}
	}

	@Autowired
	private MethodAopBean methodAopBean;

	@Autowired
	private ClassAopBean classAopBean;

	@BeforeEach
	void setUp() {
		SecurityContext.setPrincipal(WebPrincipal.ANONYMOUS);
	}

	@Test
	void interceptMethod() {
		assertThatThrownBy(() -> methodAopBean.requireAuth()).isInstanceOf(PermissionException.class);

		SecurityContext.setPrincipal(new WebPrincipal(WebPrincipal.ADMIN_ID));
		methodAopBean.requireAuth();
	}

	@Test
	void interceptClass() {
		assertThatThrownBy(() -> classAopBean.requireAuth()).isInstanceOf(PermissionException.class);

		SecurityContext.setPrincipal(new WebPrincipal(WebPrincipal.ADMIN_ID));
		classAopBean.requireAuth();
	}

	@Test
	void customException() {
		assertThatThrownBy(() -> classAopBean.customException()).isInstanceOf(DataTooBigException.class);
	}

	/**
	 * Aspect 类的这俩方法仅作为注解的载体，不会被调用，所以会拉低测试覆盖率。
	 * 这里给它调用一下，避免这种情况。
	 */
	@Test
	void fixCoverage() throws Exception {
		var instance = new AuthorizeAspect();

		var method = AuthorizeAspect.class.getDeclaredMethod("clazz");
		method.setAccessible(true);
		method.invoke(instance);

		var method2 = AuthorizeAspect.class.getDeclaredMethod("method");
		method2.setAccessible(true);
		method2.invoke(instance);
	}
}
