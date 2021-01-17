package com.kaciras.blog.infra;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * 经测试，DirectByteBuffer 的创建挺耗时的，在小数据量下性能不好；直接操作数组和 HeapByteBuffer 的性能相差不大。
 */
@State(Scope.Thread)
@Fork(1)
@Measurement(iterations = 5, time = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BytesConcatPerf {

	private static final byte[] PART0 = "inf".getBytes();
	private static final byte[] PART1 = ("From configuration to security, web apps to big data – whatever " +
			"the infrastructure needs of your application may be, there is a Spring Project to help " +
			"you build it. Start small and use just what you need – Spring is modular by design").getBytes();

	private static final int SINGLE_COUNT = 10;

	private static final int SIZE = PART0.length + PART1.length + SINGLE_COUNT;

	@Benchmark
	public byte[] bytesCopy() {
		var result = new byte[SIZE];
		System.arraycopy(PART0, 0, result, 0, PART0.length);
		for (var i = PART0.length; i < PART0.length + SINGLE_COUNT; i++) {
			result[i] = -128;
		}
		System.arraycopy(PART1, 0, result, SIZE - PART1.length, PART1.length);
		return result;
	}

	@Benchmark
	public byte[] heapByteBuffer() {
		var buffer = ByteBuffer
				.allocate(SIZE)
				.put(PART0)
				.put(PART1);
		for (int i = 0; i < SINGLE_COUNT; i++) {
			buffer.put((byte)-128);
		}
		return buffer.array();
	}

	@Benchmark
	public byte[] directByteBuffer() {
		var buffer = ByteBuffer
				.allocateDirect(SIZE)
				.put(PART0)
				.put(PART1);
		for (int i = 0; i < SINGLE_COUNT; i++) {
			buffer.put((byte)-128);
		}
		var result = new byte[SIZE];
		buffer.flip().get(result);
		return result;
	}

	public static void main(String[] args) throws RunnerException {
		var options = new OptionsBuilder()
				.include(BytesConcatPerf.class.getSimpleName())
				.build();
		new Runner(options).run();
	}
}
