package com.kaciras.blog.api.notice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class NotificationService {

	private final ListOperations<String, byte[]> redis;
	private final ObjectMapper objectMapper;

	private final MailService mailService;

	private final Set<String> types = Set.of("dz", "fr");

	Map<String, List<byte[]>> getAll() {
		return types.stream().collect(Collectors.toMap(Function.identity(), this::getAll));
	}

	List<byte[]> getAll(String type) {
		return redis.range("notice:" + type, 0, -1);
	}

	// 加上异步以便不干扰调用方的流程，如果出了异常也只限于本模块。
	@Async
	@SneakyThrows(JsonProcessingException.class)
	public void add(HttpNotice activity) {
		var type = activity.getKind();

		if (!types.contains(type)) {
			throw new RuntimeException("通知类型必须先注册: " + type);
		}

		/*
		 * 设计上 HTTP API 的消息对象类型是 JsonObject，消息对象在存储前应当转换。
		 * 但这里
		 */
		var json = objectMapper.writeValueAsBytes(activity);
		var index = redis.rightPush("notice:" + type, json);

		// 只有通知为空时才发邮件，更多内容自己去控制台看就行。
		// noinspection ConstantConditions
		var clear = index == 1;

		if (mailService != null && activity instanceof MailNotice) {
			((MailNotice) activity).sendMail(clear, mailService);
		}
	}

	public void clear() {
		types.forEach(type -> redis.getOperations().unlink("notice:" + type));
	}
}
