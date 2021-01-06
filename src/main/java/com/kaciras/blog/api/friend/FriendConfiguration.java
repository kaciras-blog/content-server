package com.kaciras.blog.api.friend;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class FriendConfiguration {

	private final TaskScheduler taskScheduler;
	private final FriendValidateService validateService;

	@Value("${app.validate-friend}")
	private boolean enable;

	// 把定时代码放到 ValidateService 外更好些，也更便于测试，ValidateService 就专注验证逻辑。
	@PostConstruct
	private void init() {
		if (enable) {
			taskScheduler.scheduleAtFixedRate(validateService::startValidation, Duration.ofDays(1));
		}
	}
}
