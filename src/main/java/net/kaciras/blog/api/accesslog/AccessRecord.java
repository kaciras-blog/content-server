package net.kaciras.blog.api.accesslog;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.net.InetAddress;
import java.time.LocalDateTime;

@Getter
@Setter
public final class AccessRecord {

	private InetAddress ip;
	private String path;
	private int statusCode;

	/** UA可以自定义，可能有些奇葩无法解析，只能直接保存，然后在分析时解析 */
	@Nullable
	private String userAgent;

	private LocalDateTime startTime;
	private long delay;
}
