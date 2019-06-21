package net.kaciras.blog.api.config;

import java.lang.annotation.*;

/**
 * 标记字段或方法需要绑定到配置项，用法：
 * {@code @BindConfig("abc") private SomeConfig config; }
 * 表示将字段 config 绑定，相当于调用了 configService.bind("abc", SomeConfig.class, newConfig -> config = newConfig);
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface BindConfig {

	/** 要绑定的配置项名，该配置项修改后将自动设置被注解的字段，或调用被注解的方法 */
	String value();
}
