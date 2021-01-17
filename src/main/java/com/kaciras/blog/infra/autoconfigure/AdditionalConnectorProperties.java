package com.kaciras.blog.infra.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.InetAddress;

@ConfigurationProperties("server.additional-connector")
@Getter
@Setter
public final class AdditionalConnectorProperties {

	/** 增加一个接受HTTP连接的端口，用于同时支持HTTP和HTTPS. */
	private int port;

	/** 监听器绑定的地址 */
	private InetAddress address;
}
