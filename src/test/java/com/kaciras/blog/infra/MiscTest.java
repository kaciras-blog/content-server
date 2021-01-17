package com.kaciras.blog.infra;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class MiscTest {

	@Test
	void getFirst() {
		var iterable = List.of(11, 13, 17, 19);
		assertThat(Misc.getFirst(iterable)).isEqualTo(11);

		assertThatThrownBy(() -> Misc.getFirst(List.of()))
				.isInstanceOf(NoSuchElementException.class);
	}

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
