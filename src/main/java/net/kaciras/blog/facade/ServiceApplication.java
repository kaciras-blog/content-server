package net.kaciras.blog.facade;

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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

@ComponentScan({"net.kaciras.blog.domain", "net.kaciras.blog.facade"})
@MapperScan(value = "net.kaciras.blog.domain", annotationClass = Mapper.class)
@EnableScheduling
@EnableTransactionManagement
@EnableWebMvc
@SpringBootApplication
public class ServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

	/**
	 * &#064;EnableScheduling 注解将自动使用 TaskScheduler 类型的bean。
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

//	@Bean
//	public EmbeddedServletContainerCustomizer tomcatCustomizer() {
//
//	}

	@Bean
	public MessageClient messageClient() {
		return new DirectCalledMessageClient();
	}

	@Bean
	CacheManager cacheManager() {
		return CacheManagerBuilder.newCacheManagerBuilder().build(true);
	}
}
