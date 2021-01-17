package com.kaciras.blog.infra;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 测试判断一个字符串（可以是null）是否是多个字符串其中之一的几种方法的性能。
 * <p>
 * Misc.isSafeRequest() 就是这样的需求，虽然几种方法内存占用不一样，但时间才是需要在意的。
 * <p>
 * Benchmark                        Mode  Cnt    Score    Error   Units
 * StringMatchPerf.hashSet         thrpt    5  168.980 ±  9.778  ops/us
 * StringMatchPerf.immutableSet    thrpt    5   66.351 ±  0.529  ops/us
 * StringMatchPerf.equals          thrpt    5   66.577 ±  0.360  ops/us
 * StringMatchPerf.regexp          thrpt    5    9.505 ±  0.085  ops/us
 * StringMatchPerf.switchCase      thrpt    5  221.765 ± 41.768  ops/us
 */
@State(Scope.Thread)
@Fork(1)
@Measurement(iterations = 5, time = 5)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class StringMatchPerf {

	// Switch 里不能用数组，必须一个个声明
	private static final String S0 = "UncheckedFunctionsTest";
	private static final String S1 = "MultipleEquals";
	private static final String S2 = "GET";
	private static final String S3 = "OPTIONS";
	private static final String S4 = "判断一个字符串";
	private static final String S5 = "ImmutableSet";
	private static final String S6 = "MILLISECONDS";
	private static final String S7 = "SwitchCase";
	private static final String S8 = "HEAD";
	private static final String S9 = "StringMatchPerf";

	private static final Set<String> immutableSet = Set.of(S0, S1, S2, S3, S4, S5, S6, S7, S8, S9);
	private static final Set<String> hashSet = new HashSet<>(immutableSet);

	private static final Pattern regex = Pattern.compile(
			new StringJoiner("|", "^(", ")$")
					.add(S0).add(S1).add(S2).add(S3).add(S4).add(S5).add(S6).add(S7).add(S8).add(S9)
					.toString()
	);

	// 不要设为 final 避免优化
	@SuppressWarnings("FieldMayBeFinal")
	private String string = S7;

	@Benchmark
	public boolean equals() {
		//@formatter:off
		return     S0.equals(string)
				|| S1.equals(string)
				|| S2.equals(string)
				|| S3.equals(string)
				|| S4.equals(string)
				|| S5.equals(string)
				|| S6.equals(string)
				|| S7.equals(string)
				|| S8.equals(string)
				|| S9.equals(string);
		//@formatter:on
	}

	@Benchmark
	public boolean switchCase() {
		if (string != null) {
			switch (string) {
				case S0:
				case S1:
				case S2:
				case S3:
				case S4:
				case S5:
				case S6:
				case S7:
				case S8:
				case S9:
					return true;
			}
		}
		return false;
	}

	@Benchmark
	public boolean immutableSet() {
		return string != null && immutableSet.contains(string);
	}

	@Benchmark
	public boolean hashSet() {
		return hashSet.contains(string);
	}

	@Benchmark
	public boolean regexp() {
		return string != null && regex.matcher(string).find();
	}

	public static void main(String[] args) throws Exception {
		var options = new OptionsBuilder()
				.include(StringMatchPerf.class.getSimpleName())
				.build();
		new Runner(options).run();
	}
}
