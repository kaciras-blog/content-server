package com.kaciras.blog.infra;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

final class MiscTest {

	// 只测了下是否触发警告
	@Test
	void disableIllegalAccessWarning() {
		var backup = System.err;
		var stdout = new ByteArrayOutputStream();
		System.setErr(new PrintStream(stdout));

		Misc.disableIllegalAccessWarning();

		assertThat(stdout.size()).isZero();
		System.setErr(backup);
	}
}
