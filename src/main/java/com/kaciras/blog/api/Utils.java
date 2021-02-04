package com.kaciras.blog.api;

import com.kaciras.blog.infra.exception.RequestArgumentException;
import com.kaciras.blog.infra.exception.ResourceNotFoundException;
import com.kaciras.blog.infra.exception.WebBusinessException;
import lombok.experimental.UtilityClass;
import org.apache.ibatis.builder.BuilderException;
import org.springframework.dao.DataAccessException;
import org.springframework.lang.NonNull;

import javax.servlet.http.HttpServletRequest;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

@UtilityClass
public class Utils {

	public void checkPositive(int value, String valname) {
		if (value <= 0) throw new RequestArgumentException("参数" + valname + "必须是正数:" + value);
	}

	public void checkNotNegative(int value, String valname) {
		if (value < 0) throw new RequestArgumentException("参数" + valname + "不能为负:" + value);
	}

	/**
	 * 从 Mybatis 的异常里提取出 SQLProvider 里的异常，如果没有则返回原异常。
	 *
	 * TODO: 要不要上 AOP？
	 *
	 * @param e 原始异常
	 * @return 提取后的异常
	 * @see org.apache.ibatis.binding.MapperRegistry
	 */
	public static RuntimeException unwrapSQLException(DataAccessException e) {
		return (RuntimeException) Optional
				.ofNullable(e.getCause())
				.filter(x -> x instanceof BuilderException)
				.map(Throwable::getCause)
				.map(Throwable::getCause)
				.filter(x -> x instanceof WebBusinessException)
				.orElse(e);
	}

	/**
	 * 获取HttpServletRequest的远程地址，处理烦人的 UnknownHostException。
	 *
	 * @param request 请求
	 * @return IP 地址，不会为 null
	 */
	@NonNull
	public InetAddress addressFromRequest(HttpServletRequest request) {
		var address = request.getRemoteAddr();
		try {
			return InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			throw new UncheckedIOException(e);
		}
	}

	public boolean isLocalNetwork(InetAddress address) {
		return address.isLoopbackAddress() || address.isSiteLocalAddress();
	}

	/**
	 * 检查Update，Delete等SQL语句是否产生了影响（影响的行数 > 0），没产生影响视为未找到
	 *
	 * @param rows 影响行数
	 * @throws ResourceNotFoundException 如果没有影响任何行
	 */
	public void checkEffective(int rows) {
		if (rows <= 0) throw new ResourceNotFoundException();
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

	/**
	 * Mybatis 的Mapper对于boolean类型的返回值，不会把空结果集转换为 false，而是 null.
	 * 像查询记录是否存在这样的需求就得绕一下。
	 *
	 * @param value 布尔对象
	 * @return 如果value是null返回false，否则返回其非装箱值。
	 */
	public boolean nullableBool(Boolean value) {
		return value != null && value;
	}
}
