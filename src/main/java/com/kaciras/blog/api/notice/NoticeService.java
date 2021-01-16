package com.kaciras.blog.api.notice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

/**
 * 面对不同类型通知包含的信息不同这个问题，有两种常见方案：
 * <ol>
 *     <li>提前渲染，在存储前就生成通知内容，统一通知类型</li>
 *     <li>动态类型，通知系统不知晓通知的类型，一律</li>
 * </ol>
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Service
public class NoticeService {

	/*
	 * 本博客的通知有以下特点：
	 * 1.只有一个面向博主的后台，无需支持多用户。
	 * 2.通知一般不会积累很多。
	 *
	 * 这些特性决定了不会有复杂的查询和大量的数据，所以可以简化存储，
	 * 直接一个 Redis List 即可无需关系型数据库。
	 */
	private final BoundListOperations<String, Notice> redis;

	private final Clock clock;
	private final ObjectMapper objectMapper;

	// Spring 的 Nullable 有跟 Autowired(required = false) 一样的效果。
	// 经测试加在字段上无效，必须在构造方法的对应参数上，但是 Delombok 的结果显示参数上没有，
	// 配置文件里也没有 lombok.copyableAnnotations，不知道怎么就通过的……
	@Nullable
	private final MailService mailService;

	public List<Notice> getAll() {
		return redis.range(0, -1);
	}

	public void clear() {
		redis.getOperations().unlink(redis.getKey());
	}

	// 加上异步以便不干扰调用方的流程，如果出了异常也只限于本模块。
	@Async
	public void add(Activity activity) {
		var data = objectMapper.valueToTree(activity);
		var notice = new Notice(activity.getActivityType(), clock.instant(), data);
		redis.rightPush(notice);

		/*
		 * 因为邮件仅通知有新回复即可，完整的通知列表在网页端，所以不需要每次都发。
		 * 发送一个邮件即表明已经通知。
		 *
		 * 在控制台里清空所有通知表示全部处理完等待新消息，此时可以发送邮件。
		 * 这里查看一下同类消息的数量来判断是否清空过，相当于 SQL 的 SELECT COUNT(*) WHERE type=?。
		 *
		 * 【其它方案】
		 * 把这个逻辑转移到前端，发送完邮件设置一个标识阻止再发，前端看完所以消息后发一个请求重置该标识。
		 */
		var typeCount = getAll().stream()
				.filter(n -> n.getType() == notice.getType())
				.count();

		if (mailService != null && activity instanceof MailNotice) {
			((MailNotice) activity).sendMail(typeCount <= 1, mailService);
		}
	}
}
