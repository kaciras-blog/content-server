package com.kaciras.blog.infra.validate;

import org.hibernate.validator.constraints.ConstraintComposition;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.CompositionType.OR;

/**
 * 验证字符串不能为空白，但可以为 null。
 * <p>
 * null 和空白的含义不同，有时会出现可以为 null 但不能为空的情况。
 */
@ConstraintComposition(OR)
@Null
@NotBlank
@ReportAsSingleViolation
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
public @interface NullOrNotBlank {

	String message() default "{org.hibernate.validator.constraints.NullOrNotBlank.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
