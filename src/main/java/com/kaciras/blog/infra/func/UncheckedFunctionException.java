package com.kaciras.blog.infra.func;

/** 仅用于包装烦人的 CheckedException，使函数式编程更舒服 */
public final class UncheckedFunctionException extends RuntimeException {

	public UncheckedFunctionException(Throwable cause) { super(cause); }
}
