package net.kaciras.blog.domain;

import net.kaciras.blog.domain.permission.PermissionKeyTypeHandler;
import net.kaciras.blog.infrastructure.codec.ImageRefrenceTypeHandler;
import net.kaciras.blog.infrastructure.codec.IpAddressTypeHandler;
import net.kaciras.blog.infrastructure.message.DirectCalledMessageClient;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.type.TypeHandler;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

@Configuration
@ComponentScan("net.kaciras.blog.domain")
@MapperScan(value = "net.kaciras.blog.domain", annotationClass = Mapper.class)
@EnableScheduling
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
public class CommonConfiguration {

	@Bean
	public static PropertySourcesPlaceholderConfigurer placeholderConfigurer(@Qualifier("config") Properties properties) {
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		configurer.setProperties(properties);
		return configurer;
	}

	/**
	 * &#064;EnableScheduling 注解将自动使用 TaskScheduler 类型的bean。
	 *
	 * @return TaskScheduler
	 */
	@Bean(destroyMethod = "destroy")
	public ThreadPoolTaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(4);
		taskScheduler.initialize();
		taskScheduler.setDaemon(true);
		taskScheduler.setThreadNamePrefix("Shud-");
		return taskScheduler;
	}

	@Bean(destroyMethod = "forceCloseAll")
	public PooledDataSource dataSource(Properties config) {
		PooledDataSource dataSource = new PooledDataSource();
		dataSource.setDriver(config.getProperty("db.driver"));
		dataSource.setUrl(config.getProperty("db.url"));
		dataSource.setUsername(config.getProperty("db.user"));
		dataSource.setPassword(config.getProperty("db.password"));
		return dataSource;
	}

	@Bean
	public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) throws IOException {
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setTypeHandlers(new TypeHandler[]{
				new IpAddressTypeHandler(),
				new PermissionKeyTypeHandler(),
				new ImageRefrenceTypeHandler()
		});
		bean.setDataSource(dataSource);
		ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
		bean.setMapperLocations(patternResolver.getResources("result-map.xml"));
		return bean;
	}

	/**
	 * &#064;EnableTransactionManagement 注解将自动使用 PlatformTransactionManager 类型的bean
	 *
	 * @param dataSource 数据源
	 * @return PlatformTransactionManager
	 */
	@Bean
	public PlatformTransactionManager txManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean
	public MessageClient messageClient() {
		return new DirectCalledMessageClient();
	}

//	@Bean
//	public MessageClient subscriber(Executor executor) {
//		LinkedEventQueue eventQueue = new LinkedEventQueue(executor);
//		return new MessageClient(eventQueue, eventQueue);
//	}

	@Bean
	CacheManager cacheManager() {
		return CacheManagerBuilder.newCacheManagerBuilder().build(true);
	}
}
