package com.kaciras.blog.infra.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 验证URI是有效的HTTP地址，即包含HTTP协议和域名两部分。
 * <p>
 * 【为什么要自己实现】
 * javax.validate 内置的 @URL 仅支持字符串，我也没找到别的注解能用在URI上的，只能自己写咯。
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
