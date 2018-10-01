package net.kaciras.blog.api.perm;

import net.kaciras.blog.api.SecurtyContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class PrincipalAspect {

	@Before("@annotation(net.kaciras.blog.api.perm.RequirePrincipal) && execution(* *(..))")
	public void beforeRequireAdministor(JoinPoint joinPoint) throws Exception {
		var annotation = ((MethodSignature) joinPoint.getSignature())
				.getMethod()
				.getAnnotation(RequirePrincipal.class);
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
