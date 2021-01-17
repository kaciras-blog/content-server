package com.kaciras.blog.infra.func;

@FunctionalInterface
public interface UncheckedRunnable extends Runnable {

	@Override
	default void run() {
		try {
			runThrows();
		} catch (Exception e) {
			throw new UncheckedFunctionException(e);
		}
	}

	void runThrows() throws Exception;
}
