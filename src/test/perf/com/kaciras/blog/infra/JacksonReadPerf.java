package com.kaciras.blog.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.*;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 有些地方比如 MockHttpServletResponse 有两种获取请求体的方法，getContentAsByteArray 和 getContentAsString；
 * 同时 ObjectMapper 也支持读取两种类型，这里比较下它们的性能。
 * <p>
 * Benchmark                          (dataFile)   Mode  Cnt     Score    Error   Units
 * JacksonReadPerf.fromBytes   package-lock.json  thrpt    5     1.340 ±  0.044  ops/ms
 * JacksonReadPerf.fromBytes          small.json  thrpt    5  1126.715 ±  7.481  ops/ms
 * JacksonReadPerf.fromString  package-lock.json  thrpt    5     1.558 ±  0.011  ops/ms
 * JacksonReadPerf.fromString         small.json  thrpt    5  1228.469 ± 51.401  ops/ms
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class JacksonReadPerf {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Param({"package-lock.json", "small.json"})
	private String dataFile;

	private byte[] data;

	@Setup
	public void loadData() throws IOException {
		data = new ClassPathResource("perf/" + dataFile).getInputStream().readAllBytes();
	}

	@Benchmark
	public Object fromString() throws IOException {
		return objectMapper.readTree(Arrays.copyOf(data, data.length));
	}

	@Benchmark
	public Object fromBytes() throws JsonProcessingException {
		return objectMapper.readTree(new String(data));
	}
}
