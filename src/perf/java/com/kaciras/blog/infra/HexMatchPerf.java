package com.kaciras.blog.infra;

import com.kaciras.blog.infra.codec.CodecUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.BitSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 对比几种检查字符串是否是HEX的方式的性能。
 * <p>
 * f1894c00ba-default-非英文字符:
 * Benchmark                         Mode    Cnt   Score   Error   Units
 * HexMatchPerf.bitSet               thrpt    5   2.941 ± 0.151  ops/us
 * HexMatchPerf.bySwitch             thrpt    5   2.325 ± 0.191  ops/us
 * HexMatchPerf.clr                  thrpt    5   3.582 ± 0.210  ops/us
 * HexMatchPerf.ifRange              thrpt    5   4.030 ± 0.158  ops/us
 * HexMatchPerf.regexp               thrpt    5   1.779 ± 0.005  ops/us
 * HexMatchPerf.switchExpression     thrpt    5   2.550 ± 0.188  ops/us
 * <p>
 * 0de735be2d228599d4a48fe37f7cdc45b6134296a9bd59959590f7cefffeaf96:
 * Benchmark                         Mode    Cnt   Score   Error   Units
 * HexMatchPerf.bitSet               thrpt    5   8.857 ± 0.475  ops/us
 * HexMatchPerf.bySwitch             thrpt    5   7.451 ± 0.449  ops/us
 * HexMatchPerf.clr                  thrpt    5   7.544 ± 0.114  ops/us
 * HexMatchPerf.ifRange              thrpt    5   9.845 ± 0.590  ops/us
 * HexMatchPerf.regexp               thrpt    5   6.245 ± 0.013  ops/us
 * HexMatchPerf.switchExpression     thrpt    5  10.674 ± 1.342  ops/us
 */
@State(Scope.Thread)
@Fork(1)
@Measurement(iterations = 5, time = 5)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class HexMatchPerf {

	private static final Pattern REGEX = Pattern.compile("^[0-9a-fA-F]+$");
	private static final BitSet BIT_SET = new BitSet();

	static {
		for (char c = '0'; c <= '9'; c++) BIT_SET.set(c);
		for (char c = 'a'; c <= 'z'; c++) BIT_SET.set(c);
		for (char c = 'A'; c <= 'Z'; c++) BIT_SET.set(c);
	}

	@SuppressWarnings("unused")
	@Param({"f1894c00ba-default-非英文字符", "0de735be2d228599d4a48fe37f7cdc45b6134296a9bd59959590f7cefffeaf96"})
	private String text;

	@Benchmark
	public void ifRange(Blackhole blackhole) {
		for (var c : text.toCharArray()) {
			blackhole.consume(CodecUtils.isHexDigit(c));
		}
	}

	@Benchmark
	public void switchExpression(Blackhole blackhole) {
		for (var c : text.toCharArray()) {
			switch (c) {
				case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
						'a', 'b', 'c', 'd', 'e', 'f',
						'A', 'B', 'C', 'D', 'E', 'F' -> blackhole.consume(true);
				default -> blackhole.consume(false);
			}
		}
	}

	@SuppressWarnings("EnhancedSwitchMigration")
	@Benchmark
	public void bySwitch(Blackhole blackhole) {
		for (var c : text.toCharArray()) {
			switch (c) {
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case 'a':
				case 'b':
				case 'c':
				case 'd':
				case 'e':
				case 'f':
				case 'A':
				case 'B':
				case 'C':
				case 'D':
				case 'E':
				case 'F':
					blackhole.consume(true);
			}
			blackhole.consume(false);
		}
	}

	@Benchmark
	public void regexp(Blackhole blackhole) {
		blackhole.consume(REGEX.matcher(text).find());
	}

	@Benchmark
	public void clr(Blackhole blackhole) {
		for (var c : text.toCharArray()) {
			blackhole.consume(Character.digit(c, 16) != -1);
		}
	}

	@Benchmark
	public void bitSet(Blackhole blackhole) {
		for (var c : text.toCharArray()) {
			blackhole.consume(BIT_SET.get(c));
		}
	}

	public static void main(String[] args) throws RunnerException {
		var options = new OptionsBuilder()
				.include(HexMatchPerf.class.getSimpleName())
				.build();
		new Runner(options).run();
	}
}
