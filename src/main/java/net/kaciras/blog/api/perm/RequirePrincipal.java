package net.kaciras.blog.api.perm;

import net.kaciras.blog.infrastructure.exception.PermissionException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePrincipal {

	WebPrincipalType value() default WebPrincipalType.Administor;

	Class<? extends Exception> ex() default PermissionException.class;
}
