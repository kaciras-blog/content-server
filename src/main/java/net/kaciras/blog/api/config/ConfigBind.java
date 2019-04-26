package net.kaciras.blog.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记字段或方法需要绑定到配置项，用法：
 * {@code @ConfigBind("abc") }
 * {@code int property = 123; }
 * 表示将字段 property 绑定到 abc 配置项上，其具有默认值 123。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface ConfigBind {

	/** 要绑定的配置项名，该配置项修改后将自动设置被注解的字段，或调用被注解的方法 */
	String value();
}
