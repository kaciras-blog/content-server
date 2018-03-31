package net.kaciras.blog.domain.accesslog;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.net.InetAddress;
import java.time.LocalDateTime;

@Getter
@Setter
public class AccessRecord implements Serializable {

	private InetAddress address;
	private String path;
	private int statusCode;

	private String referer;
	private String browser;
	private String browserVersion;
	private String system;

	private LocalDateTime time;
}
