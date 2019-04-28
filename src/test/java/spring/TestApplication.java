package spring;

import net.kaciras.blog.infrastructure.autoconfig.KxCodecAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

/**
 * ComponentScan注解排除了意外扫描到其它测试配置的情况。
 */
@EnableLoadTimeWeaving
@Import(KxCodecAutoConfiguration.class)
@SpringBootApplication
@ComponentScan(excludeFilters = @Filter(type = FilterType.ANNOTATION, value = SpringBootApplication.class))
public class TestApplication {}
