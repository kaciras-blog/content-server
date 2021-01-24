package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.user.UserManager;
import com.kaciras.blog.api.user.UserVo;
import com.kaciras.blog.infra.autoconfigure.KxCodecAutoConfiguration;
import org.openjdk.jmh.annotations.*;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@Measurement(iterations = 5, time = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DiscussionQueryPerf {

	private ConfigurableApplicationContext context;

	private DiscussionRepository repository;
	private ViewModelMapper mapper;

	private DiscussionQuery qmode;
	private DiscussionQuery nmode;

	@Setup
	public void setUp() {
		context = new SpringApplicationBuilder(SpringConfig.class)
				.profiles("test")
				.web(WebApplicationType.NONE)
				.run();

		mapper = context.getBean(ViewModelMapper.class);
		repository = context.getBean(DiscussionRepository.class);

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

	@TearDown
	public void close() {
		context.close();
	}

	@Benchmark
	public Object quoteMode() {
		return new QueryCacheSession(repository, mapper).execute(qmode);
	}

	@Benchmark
	public Object nestMode() {
		return new QueryCacheSession(repository, mapper).execute(nmode);
	}

	@Import(KxCodecAutoConfiguration.class)
	@EnableAutoConfiguration
	@TestConfiguration(proxyBeanMethods = false)
	private static final class SpringConfig {

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
