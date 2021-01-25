package com.kaciras.blog.api.discuss;

import com.kaciras.blog.AbstractSpringPerf;
import com.kaciras.blog.api.user.UserManager;
import com.kaciras.blog.api.user.UserVo;
import com.kaciras.blog.infra.autoconfigure.KxCodecAutoConfiguration;
import org.openjdk.jmh.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

@ContextConfiguration(classes = DiscussionQueryPerf.SpringConfig.class)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@Measurement(iterations = 5, time = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DiscussionQueryPerf extends AbstractSpringPerf {

	@Autowired
	private DiscussionRepository repository;

	@Autowired
	private ViewModelMapper mapper;

	private DiscussionQuery qmode;
	private DiscussionQuery nmode;

	@Setup
	public void setUp2() {
		var page = PageRequest.of(0, 20);

		qmode = new DiscussionQuery()
				.setIncludeParent(true)
				.setType(1)
				.setObjectId(1)
				.setPageable(page);

		nmode = new DiscussionQuery()
				.setChildCount(5)
				.setType(1)
				.setObjectId(1)
				.setPageable(page);
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
	@Import(KxCodecAutoConfiguration.class)
	@EnableAutoConfiguration
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
		public UserVo getUser(int id) {
			return null;
		}
	}
}
