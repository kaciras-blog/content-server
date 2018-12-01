package another.pkg;

import net.kaciras.blog.infrastructure.autoconfig.KxCodecAutoConfiguration;
import net.kaciras.blog.infrastructure.message.DirectMessageClient;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;

/**
 * ComponentScan注解排除了意外扫描到其它测试配置的情况。
 */
@EnableLoadTimeWeaving
@Import(KxCodecAutoConfiguration.class)
@SpringBootApplication
@ComponentScan(excludeFilters = @Filter(type = FilterType.ANNOTATION, value = SpringBootApplication.class))
public class TestApplication {

	@Bean
	MessageClient messageClient() {
		return new DirectMessageClient();
	}
}
