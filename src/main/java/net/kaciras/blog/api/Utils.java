package net.kaciras.blog.api;

import lombok.experimental.UtilityClass;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import org.springframework.lang.NonNull;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

@UtilityClass
public class Utils {

	public void checkPositive(int value, String valname) {
		if (value <= 0) throw new RequestArgumentException("参数" + valname + "必须是正数:" + value);
	}

	public void checkNotNegative(int value, String valname) {
		if (value < 0) throw new RequestArgumentException("参数" + valname + "不能为负:" + value);
	}

	/**
	 * 获取HttpServletRequest的远程地址。
	 * 该方法为SpringMVC特别处理，解决Mock请求的地址为空问题，以及烦人的UnknownHostException。
	 *
	 * @param request 请求
	 * @return IP地址，不会为null
	 */
	@NonNull
	public InetAddress AddressFromRequest(HttpServletRequest request) {
		var addr = request.getRemoteAddr();

		// 没有地址说明是Mock来的请求
		if (addr == null) {
			return InetAddress.getLoopbackAddress();
		}

		// 这里认为 HttpServletRequest.getRemoteAddr() 获取的格式一定是对的
		try {
			return InetAddress.getByName(addr);
		} catch (UnknownHostException e) {
			throw new AssertionError("HttpServletRequest竟然返回无效的地址");
		}
	}

	/**
	 * Mybatis 的Mapper对于boolean类型的返回值，不会把空结果集转换为 false，而是 null.
	 * 像查询记录是否存在这样的需求就得绕一下。
	 *
	 * @param value 布尔对象
	 * @return 如果value是null返回false，否则返回其非装箱值。
	 */
	public boolean nullableBool(Boolean value) {
		return value == null ? false : value;
	}
}
