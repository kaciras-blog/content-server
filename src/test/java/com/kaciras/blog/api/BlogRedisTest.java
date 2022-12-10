package com.kaciras.blog.api;

import com.kaciras.blog.infra.autoconfigure.RedisUtilsAutoConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.AutoConfigureDataRedis;
import org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.*;

@ActiveProfiles("test")
@TestExecutionListeners(value = AutoFlushRedis.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Import(RedisUtilsAutoConfiguration.class)
@AutoConfigureJson

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited

@BootstrapWith(SpringBootTestContextBootstrapper.class)

@ExtendWith(SpringExtension.class)
@OverrideAutoConfiguration(enabled = false)
@TypeExcludeFilters(DisableScanFilter.class)
@AutoConfigureDataRedis
@ImportAutoConfiguration
public @interface BlogRedisTest {


}

class DisableScanFilter extends TypeExcludeFilter {

	@Override
	public boolean match(MetadataReader r, MetadataReaderFactory f) {
		return true;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj != null) && (getClass() == obj.getClass());
	}
}

class AutoFlushRedis implements TestExecutionListener {

	@Override
	public void beforeTestExecution(TestContext testContext) {
		var ctx = testContext.getApplicationContext();
		var redis = ctx.getBean(RedisConnectionFactory.class);
		try (var connection = redis.getConnection()) {
			connection.serverCommands().flushDb();
		}
	}
}
