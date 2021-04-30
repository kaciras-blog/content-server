package com.kaciras.blog.infra;

import com.kaciras.blog.infra.exception.HttpStatusException;
import org.apache.ibatis.builder.BuilderException;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Optional;

/**
 * 对 Mybatis Mapper 的扩展，因为 Mybatis 写死了 Mapper 的创建逻辑所以只能用 AOP 来做了。
 *
 * @see org.apache.ibatis.binding.MapperRegistry
 */
@Aspect
public final class MybatisMapperAspect {

	@Pointcut("@within(org.apache.ibatis.annotations.Mapper)")
	private void mapper() {}

	/**
	 * 目前只用了 SelectProvider，其它以后再加。
	 */
	@Pointcut("mapper() && @annotation(org.apache.ibatis.annotations.SelectProvider)")
	private void useProvider() {}

	/**
	 * 从 Mybatis 的异常里提取出 SQLProvider 里的异常，如果没有则返回原异常。
	 *
	 * @param e 原始异常
	 * @throws Throwable 提取后的异常
	 */
	@AfterThrowing(value = "useProvider()", throwing = "e")
	public void handle(Throwable e) throws Throwable {
		throw Optional
				.ofNullable(e.getCause())
				.filter(x -> x instanceof BuilderException)
				.map(Throwable::getCause)
				.map(Throwable::getCause)
				.filter(x -> x instanceof HttpStatusException)
				.orElse(e);
	}
}
