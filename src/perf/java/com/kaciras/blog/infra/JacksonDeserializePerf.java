package com.kaciras.blog.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测下Jackson对构造方法、公共字段赋值、setter三种注入方式的性能差别。
 * <p>
 * 结果构造方法竟然比字段还快些……
 * <p>
 * 另外发现两个坑：
 * 1）Jackson 对构造方法参数名不是内置支持，需要 jackson-modules-java8  这个模块。
 * 2）setter 的字段名有讲究，vString 这种无法识别。
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Measurement(iterations = 5, time = 10)
@State(Scope.Benchmark)
public class JacksonDeserializePerf {

	@SuppressWarnings("unused")
	public static final class PublicFields {
		public boolean boolValue;
		public byte byteValue;
		public short shortValue;
		public int intValue;
		public long longValue;
		public float floatValue;
		public double doubleValue;
		public String stringValue;

		public int i1;
		public int i2;
		public int i3;
		public int i4;
		public int i5;
		public int i6;
		public int i7;
		public int i8;
	}

	@AllArgsConstructor
	public static final class Constructor {
		public final boolean boolValue;
		public final byte byteValue;
		public final short shortValue;
		public final int intValue;
		public final long longValue;
		public final float floatValue;
		public final double doubleValue;
		public final String stringValue;

		public final int i1;
		public final int i2;
		public final int i3;
		public final int i4;
		public final int i5;
		public final int i6;
		public final int i7;
		public final int i8;
	}

	@Getter
	@Setter
	public static final class Setters {
		private boolean boolValue;
		private byte byteValue;
		private short shortValue;
		private int intValue;
		private long longValue;
		private float floatValue;
		private double doubleValue;
		private String stringValue;

		private int i1;
		private int i2;
		private int i3;
		private int i4;
		private int i5;
		private int i6;
		private int i7;
		private int i8;
	}

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String json;

	@Setup
	public void setUp() throws Exception {
		objectMapper.findAndRegisterModules();
		var object = new Constructor(true, (byte) 2, (short) 3, 4, 5L, 6.5F, 7.0, "testValue", 6, 2, 5, 0, 8, 9, 3, 4);
		json = objectMapper.writeValueAsString(object);

		var c = objectMapper.readValue(json, Constructor.class);
		var f = objectMapper.readValue(json, PublicFields.class);
		var s = objectMapper.readValue(json, Setters.class);

		assertThat(c).isEqualToComparingFieldByField(f);
		assertThat(f).isEqualToComparingFieldByField(s);
		assertThat(f).isEqualToComparingFieldByField(object);
	}

	@Benchmark
	public Object ctor() throws Exception {
		return objectMapper.readValue(json, Constructor.class);
	}

	@Benchmark
	public Object fields() throws Exception {
		return objectMapper.readValue(json, PublicFields.class);
	}

	@Benchmark
	public Object setters() throws Exception {
		return objectMapper.readValue(json, Setters.class);
	}

	public static void main(String[] args) throws Exception {
		var options = new OptionsBuilder()
				.include(JacksonDeserializePerf.class.getSimpleName())
				.build();
		new Runner(options).run();
	}
}
