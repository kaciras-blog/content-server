package net.kaciras.blog.api.perm;

import net.kaciras.blog.api.SecurtyContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * 对注解 RequirePrincipal 的方法或类中的方法在调用前进行鉴权。
 * 权限比较简单的时候，直接用注解来过滤更方便，但权限比较复杂时还是
 * 需要些更详细的手动鉴权。
 */
@Aspect
public final class PrincipalAspect {

	@Pointcut("@within(net.kaciras.blog.api.perm.RequirePrincipal)")
	private void clazz() {}

	@Pointcut("@annotation(net.kaciras.blog.api.perm.RequirePrincipal)")
	private void method() {}

	/**
	 * 类上存在注解但方法上不存在时，以类上的注解来鉴权。
	 *
	 * @param joinPoint 切点
	 * @throws Exception 如果鉴权失败则抛出异常。
	 */
	@Before("clazz() && !method() && execution(* *(..))")
	public void beforeClass(JoinPoint joinPoint) throws Exception {
		var annotation = (RequirePrincipal)joinPoint.getSignature()
				.getDeclaringType()
				.getDeclaredAnnotation(RequirePrincipal.class);
		check(annotation);
	}

	/**
	 * 方法上存在注解，需要鉴权。
	 *
	 * @param joinPoint 切点
	 * @throws Exception 如果鉴权失败则抛出异常。
	 */
	@Before("method() && execution(* *(..))")
	public void beforeMethod(JoinPoint joinPoint) throws Exception {
		var annotation = ((MethodSignature) joinPoint.getSignature())
				.getMethod()
				.getDeclaredAnnotation(RequirePrincipal.class);
		check(annotation);
	}

	private void check(RequirePrincipal annotation) throws Exception {
		boolean passed;

		switch (annotation.value()) {
			case System:
				passed = SecurtyContext.getPrincipal().isSystem();
				break;
			case Administor:
				passed = SecurtyContext.getPrincipal().isAdministor();
				break;
			case Logined:
				passed = SecurtyContext.getPrincipal().isLogined();
				break;
			default:
				passed = true; // anynomous
		}
		if (passed) {
			return;
		}
		throw annotation.ex().getConstructor().newInstance();
	}
}
