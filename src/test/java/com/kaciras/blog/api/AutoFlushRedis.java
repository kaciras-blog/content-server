package com.kaciras.blog.api;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

class AutoFlushRedis implements TestExecutionListener {

	@Override
	public void beforeTestExecution(TestContext testContext) {
		var ctx = testContext.getApplicationContext();
		try {
			var redis = ctx.getBean(RedisConnectionFactory.class);
			try (var connection = redis.getConnection()) {
				connection.serverCommands().flushDb();
			}
		} catch (NoSuchBeanDefinitionException ignore) {

		}
	}
}
