package net.kaciras.blog.facade;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.kaciras.blog.domain.permission.PermissionKeyTypeHandler;
import net.kaciras.blog.infrastructure.bootstarp.CommandListener;
import net.kaciras.blog.infrastructure.codec.ExtendsCodecModule;
import net.kaciras.blog.infrastructure.codec.ImageRefrenceTypeHandler;
import net.kaciras.blog.infrastructure.codec.IpAddressTypeHandler;
import net.kaciras.blog.infrastructure.message.DirectCalledMessageClient;
import net.kaciras.blog.infrastructure.message.JacksonJsonCodec;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.message.TcpTransmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.type.TypeHandler;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.concurrent.Executors;

@ComponentScan({"net.kaciras.blog.domain", "net.kaciras.blog.facade"})
@MapperScan(value = "net.kaciras.blog.domain", annotationClass = Mapper.class)
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableWebMvc
@SpringBootApplication
public class ServiceApplication {

	public static void main(String[] args) throws IOException {
		ConfigurableApplicationContext context = SpringApplication.run(ServiceApplication.class, args);
		CommandListener listener = new CommandListener(60002);
		listener.onShutdown(() -> SpringApplication.exit(context, () -> 0));
		listener.start();
	}

	/**
	 * EnableScheduling 注解将自动使用 TaskScheduler 类型的bean。
	 *
	 * @return TaskScheduler
	 */
	@Bean(destroyMethod = "destroy")
	ThreadPoolTaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(4);
		taskScheduler.initialize();
		taskScheduler.setDaemon(true);
		taskScheduler.setThreadNamePrefix("Shud-");
		return taskScheduler;
	}

	@Bean
	SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) throws IOException {
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
	 * SpringBoot会把Module类型的bean加入到Jackson的模块中。
	 *
	 * @return 基础层项目中包含的一些编解码模块
	 */
	@Bean
	ExtendsCodecModule extendsCodecModule() {
		return new ExtendsCodecModule();
	}

	@Bean
	MessageClient messageClient(ObjectMapper objectMapper) throws IOException {
		objectMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false).configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
//		JacksonJsonCodec codec = new JacksonJsonCodec(objectMapper);
//		TcpTransmission transmission = new TcpTransmission("123.206.206.29", 2380, codec, Executors.newSingleThreadExecutor());
		return new DirectCalledMessageClient();
	}

	@Bean
	CacheManager cacheManager() {
		return CacheManagerBuilder.newCacheManagerBuilder().build(true);
	}
}
