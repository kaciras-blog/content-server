package com.kaciras.blog.api.config;

import javax.validation.ValidationException;

/**
 * 配置类实现该接口，表示其需要在被使用前校验。
 */
public interface ValidatedConfig {

	/**
	 * 校验该配置，返回 false 或抛出 ValidationException 表示该配置对象不合法。
	 *
	 * @return 该配置对象是否合法
	 * @throws ValidationException 如果不合法将抛出此异常
	 */
	boolean validate() throws ValidationException;
}
