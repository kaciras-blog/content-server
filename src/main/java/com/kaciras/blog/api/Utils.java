package com.kaciras.blog.api;

import com.kaciras.blog.infra.exception.RequestArgumentException;
import com.kaciras.blog.infra.exception.ResourceNotFoundException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

	public void checkPositive(int value, String valname) {
		if (value <= 0)
			throw new RequestArgumentException("参数" + valname + "必须是正数:" + value);
	}

	public void checkNotNegative(int value, String valname) {
		if (value < 0)
			throw new RequestArgumentException("参数" + valname + "不能为负:" + value);
	}

	/**
	 * 检查Update，Delete等SQL语句是否产生了影响（影响的行数 > 0），没产生影响视为未找到
	 *
	 * @param rows 影响行数
	 * @throws ResourceNotFoundException 如果没有影响任何行
	 */
	public void checkEffective(int rows) {
		if (rows <= 0)
			throw new ResourceNotFoundException();
	}

	/**
	 * 检查对象是否为null，如果是则抛出ResourceNotFoundException异常。
	 *
	 * @param obj 对象
	 * @param <T> 对象类型
	 * @return 原样返回参数obj
	 */
	public <T> T checkNotNullResource(T obj) {
		if (obj == null)
			throw new ResourceNotFoundException();
		return obj;
	}
}
