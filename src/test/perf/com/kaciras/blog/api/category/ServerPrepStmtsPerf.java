package com.kaciras.blog.api.category;

import com.kaciras.blog.infra.autoconfigure.BlogMybatisAutoConfiguration;
import org.openjdk.jmh.annotations.*;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 数据库驱动里看到个 useServerPrepStmts 选项，试下能不能提升性能。
 * <p>
 * Benchmark                       Mode  Cnt  Score   Error  Units
 * ServerPrepStmtsPerf.clientSide  avgt   25  0.228 ± 0.006  ms/op
 * ServerPrepStmtsPerf.serverSide  avgt   25  0.198 ± 0.002  ms/op
 * <p>
 * 结论：蚊子肉没卵用。
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ServerPrepStmtsPerf {

	private ConfigurableApplicationContext clientContext;
	private ConfigurableApplicationContext serverContext;

	// CategoryDAO.selectPathToAncestor 是本项目最复杂的 SQL

	private CategoryDAO clientPreparedDAO;
	private CategoryDAO serverPreparedDAO;

	@Setup
	public void setUp() throws Exception {
		var default_ = new SpringApplicationBuilder(SpringConfig.class)
				.profiles("test")
				.web(WebApplicationType.NONE)
				.run();
		var url = default_.getEnvironment().getProperty("spring.datasource.url");
		default_.close();

		if(url == null){
			throw new Exception("未指定数据库连接，无法运行该测试");
		}
		url = url.substring("jdbc:".length());
		var builder = UriComponentsBuilder.fromUriString(url);

		clientContext = startApp(builder, false);
		serverContext = startApp(builder, true);

		clientPreparedDAO = clientContext.getBean(CategoryDAO.class);
		serverPreparedDAO = serverContext.getBean(CategoryDAO.class);
	}

	private ConfigurableApplicationContext startApp(UriComponentsBuilder uri, boolean serverSide) {
		var sArg = uri
				.replaceQueryParam("useServerPrepStmts", Boolean.toString(serverSide))
				.build().toString();
		sArg = "--spring.datasource.url=jdbc:"+ sArg;
		return new SpringApplicationBuilder(SpringConfig.class).profiles("test").web(WebApplicationType.NONE).run(sArg);
	}

	@TearDown
	public void tearDown(){
		clientContext.close();
		serverContext.close();
	}

	@Benchmark
	public Object clientSide() {
		return clientPreparedDAO.selectPathToAncestor(1, 666);
	}

	@Benchmark
	public Object serverSide() {
		return serverPreparedDAO.selectPathToAncestor(1, 666);
	}

	@Import(BlogMybatisAutoConfiguration.class)
	@EnableAutoConfiguration
	@TestConfiguration(proxyBeanMethods = false)
	static class SpringConfig {}
}
