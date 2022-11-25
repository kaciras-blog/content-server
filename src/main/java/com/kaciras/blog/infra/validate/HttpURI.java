package com.kaciras.blog.infra.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 验证 URI 是有效的 HTTP 地址，即包含 HTTP 协议和域名两部分。
 *
 * <h2>为什么要自己实现</h2>
 * org.hibernate.validator.constraints.URL 仅支持字符串，我也没找到能用在 URI 上的注解。
 */
@Constraint(validatedBy = HttpURIValidator.class)
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface HttpURI {

	String message() default "Invalid HTTP URI";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
