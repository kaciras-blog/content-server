package com.kaciras.blog.api.discuss;

import com.kaciras.blog.AbstractSpringPerf;
import com.kaciras.blog.api.user.UserManager;
import com.kaciras.blog.api.user.UserVO;
import com.kaciras.blog.infra.autoconfigure.HttpClientAutoConfiguration;
import org.openjdk.jmh.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Clock;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark                      Mode  Cnt  Score   Error  Units
 * DiscussionQueryPerf.nestMode   avgt   25  9.200 ± 0.167  ms/op
 * DiscussionQueryPerf.quoteMode  avgt   25  1.653 ± 0.056  ms/op
 *
 * DiscussionQueryPerf.quoteMode  avgt   25  1.001 ± 0.101  ms/op
 *
 * CTE + ROW_NUMBER() 方式 65.069 ± 26.782 ms/op 反而更慢。
 * 楼中楼单独一个方法查询能快 1ms 没啥意义。
 */
@ContextConfiguration(classes = DiscussionQueryPerf.SpringConfig.class)
@State(Scope.Benchmark)
@Measurement(iterations = 5, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DiscussionQueryPerf extends AbstractSpringPerf {

	@Autowired
	private DiscussionRepository repository;

	@Autowired
	private ViewModelMapper mapper;

	@Autowired
	private DataSource dataSource;

	private DiscussionQuery nmode;
	private DiscussionQuery qmode;

	@Setup
	public void setup() throws SQLException, IOException {
		nmode = new DiscussionQuery()
				.setChildCount(5)
				.setType(1)
				.setObjectId(2)
				.setPageable(PageRequest.of(0, 30));

		var sort = Sort.by(Sort.Order.desc("id"));
		var page2 = PageRequest.of(0, 30, sort);
		qmode = new DiscussionQuery()
				.setIncludeParent(true)
				.setType(1)
				.setObjectId(1)
				.setPageable(page2);

		new DataGenerator(this).insertTestData();
	}

	public void closeSpringContext() throws Exception {
		try (var connection = dataSource.getConnection()) {
			var stat = connection.createStatement();
			stat.execute("TRUNCATE discussion");
			stat.close();
			connection.commit();
		}
		super.closeSpringContext(); // 别忘了调用父方法
	}

	@Benchmark
	public Object quoteMode() {
		return new QueryCacheSession(repository, mapper).execute(qmode);
	}

	@Benchmark
	public Object nestMode() {
		return new QueryCacheSession(repository, mapper).execute(nmode);
	}

	// 与 Configuration 不同，TestConfiguration 不会被自动扫描到而干扰其它测试
	@EnableAutoConfiguration(exclude = HttpClientAutoConfiguration.class)
	@TestConfiguration(proxyBeanMethods = false)
	static class SpringConfig {

		@Bean
		UserManager userManager() {
			return new UserManagerStub();
		}

		@Bean
		ViewModelMapper viewModelMapper() {
			return new ViewModelMapperImpl();
		}

		@Bean
		DiscussionRepository repository(DiscussionDAO dao) {
			return new DiscussionRepository(dao, Clock.systemUTC());
		}
	}

	private static final class UserManagerStub extends UserManager {

		public UserManagerStub() {
			super(null, null);
		}

		@Override
		public UserVO getUser(int id) {
			return null;
		}
	}
}
